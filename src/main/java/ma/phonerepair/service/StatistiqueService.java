package ma.phonerepair.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import ma.phonerepair.dao.CreditDAO;
import ma.phonerepair.dao.StatistiqueDAO;
import ma.phonerepair.dao.TransactionManager;
import ma.phonerepair.model.Credit;
import ma.phonerepair.model.Intervention;
import ma.phonerepair.model.PerformanceReparateur;
import ma.phonerepair.model.ResumeCaisse;
import ma.phonerepair.model.Statistiques;
import ma.phonerepair.model.Utilisateur;
import ma.phonerepair.model.enums.Periode;
import ma.phonerepair.model.enums.SensCredit;
import ma.phonerepair.service.exception.ValidationException;
import ma.phonerepair.util.DateUtil;
import ma.phonerepair.util.MoneyUtil;

/**
 * Statistiques jour / semaine / mois / total (EF-21) et performance par réparateur (EF-22).
 * Définitions : business-rules.md § 9.2 et § 13.
 */
public class StatistiqueService {

    private final StatistiqueDAO statistiqueDAO = new StatistiqueDAO();
    private final CreditDAO creditDAO = new CreditDAO();
    private final CaisseService caisseService = new CaisseService();

    /** Bornes [début ; fin[ de la période contenant {@code jour} ; null, null pour TOTAL. */
    public static LocalDateTime[] bornes(Periode periode, LocalDate jour) {
        return switch (periode) {
            case JOUR -> new LocalDateTime[] {DateUtil.debutJour(jour), DateUtil.debutJour(jour).plusDays(1)};
            case SEMAINE -> new LocalDateTime[] {DateUtil.debutSemaine(jour), DateUtil.debutSemaine(jour).plusWeeks(1)};
            case MOIS -> new LocalDateTime[] {DateUtil.debutMois(jour), DateUtil.debutMois(jour).plusMonths(1)};
            case TOTAL -> new LocalDateTime[] {null, null};
        };
    }

    /**
     * Agrège les indicateurs d'activité à partir des interventions des appareils livrés.
     * Calcul pur (sans base), testé unitairement.
     */
    public static void calculerActivite(Statistiques s, List<Intervention> livrees) {
        BigDecimal facture = MoneyUtil.ZERO;
        BigDecimal cout = MoneyUtil.ZERO;
        BigDecimal margePieces = MoneyUtil.ZERO;
        BigDecimal mo = MoneyUtil.ZERO;
        for (Intervention i : livrees) {
            facture = facture.add(MoneyUtil.arrondir(i.getMontantClient()));
            cout = cout.add(MoneyUtil.arrondir(i.getCoutAchatPieces()));
            margePieces = margePieces.add(CommissionService.calculerMargePieces(i.getPrixPiecesClient(), i.getCoutAchatPieces()));
            mo = mo.add(MoneyUtil.arrondir(i.getMainOeuvre()));
        }
        BigDecimal commissions = CommissionService.totalCommissions(livrees);
        BigDecimal partProprietaire = mo.subtract(commissions);
        s.setMontantFacture(facture);
        s.setCoutAchatPieces(cout);
        s.setMargePieces(margePieces);
        s.setMainOeuvre(mo);
        s.setCommissions(commissions);
        s.setPartProprietaireMO(partProprietaire);
        s.setMargeBruteBoutique(margePieces.add(partProprietaire));
    }

    public Statistiques calculer(Utilisateur acteur, Periode periode, LocalDate jour) {
        Autorisation.exigerProprietaire(acteur);
        if (periode == null) {
            throw new ValidationException("Choisissez une période.");
        }
        LocalDate reference = jour == null ? LocalDate.now() : jour;
        LocalDateTime[] b = bornes(periode, reference);
        LocalDateTime debut = b[0];
        LocalDateTime fin = b[1];

        Statistiques s = TransactionManager.lecture(conn -> {
            Statistiques st = new Statistiques();
            st.setPeriode(periode);
            st.setDebut(debut);
            st.setFin(fin);
            st.setNombreReparations(statistiqueDAO.compterReparations(conn, debut, fin));
            st.setNombreAppareilsLivres(statistiqueDAO.compterAppareilsLivres(conn, debut, fin));
            st.setTotalEncaisse(statistiqueDAO.totalEncaisse(conn, debut, fin));
            List<Intervention> livrees = statistiqueDAO.interventionsLivrees(conn, debut, fin);
            calculerActivite(st, livrees);
            st.setPerformances(performances(livrees, statistiqueDAO.appareilsLivresParReparateur(conn, debut, fin),
                    statistiqueDAO.nomsReparateurs(conn)));
            st.setResteAEncaisser(statistiqueDAO.resteAEncaisserGlobal(conn));
            List<Credit> credits = creditDAO.listerTous(conn);
            st.setCreditsANousDus(CreditService.totalRestant(credits, SensCredit.ACCORDE));
            st.setCreditsQueNousDevons(CreditService.totalRestant(credits, SensCredit.RECU));
            return st;
        });
        s.setSoldeCaisses(caisseService.toutes(acteur).stream().map(ResumeCaisse::getSolde)
                .reduce(MoneyUtil.ZERO, BigDecimal::add));
        return s;
    }

    private static List<PerformanceReparateur> performances(List<Intervention> livrees, Map<Integer, Integer> appareils,
                                                            Map<Integer, String> noms) {
        Map<Integer, PerformanceReparateur> parId = new java.util.LinkedHashMap<>();
        noms.forEach((id, nom) -> parId.put(id, new PerformanceReparateur(id, nom)));
        for (Intervention i : livrees) {
            PerformanceReparateur p = parId.computeIfAbsent(i.getReparateurId(),
                    id -> new PerformanceReparateur(id, "Réparateur #" + id));
            p.setMontantFacture(p.getMontantFacture().add(MoneyUtil.arrondir(i.getMontantClient())));
            p.setMainOeuvre(p.getMainOeuvre().add(MoneyUtil.arrondir(i.getMainOeuvre())));
            p.setCommission(p.getCommission().add(
                    CommissionService.calculerCommission(i.getMainOeuvre(), i.getTauxCommissionApplique())));
        }
        appareils.forEach((id, n) -> {
            if (parId.containsKey(id)) {
                parId.get(id).setAppareilsLivres(n);
            }
        });
        return new java.util.ArrayList<>(parId.values());
    }
}
