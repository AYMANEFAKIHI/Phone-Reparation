package ma.phonerepair.service;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import ma.phonerepair.dao.CaisseDAO;
import ma.phonerepair.dao.TransactionManager;
import ma.phonerepair.model.MouvementCaisse;
import ma.phonerepair.model.ResumeCaisse;
import ma.phonerepair.model.Utilisateur;
import ma.phonerepair.model.enums.TypeMouvement;
import ma.phonerepair.service.exception.AccesRefuseException;
import ma.phonerepair.service.exception.BusinessException;
import ma.phonerepair.service.exception.ValidationException;
import ma.phonerepair.util.MoneyUtil;
import ma.phonerepair.util.ValidationUtil;

/**
 * Caisses des réparateurs (business-rules § 9, RG-36 à RG-39).
 * Une caisse = l'argent physiquement détenu par un réparateur. Solde calculé, jamais stocké.
 */
public class CaisseService {

    private final CaisseDAO caisseDAO = new CaisseDAO();
    private final CommissionService commissionService = new CommissionService();

    /**
     * Solde = Σ montants × sens du type (entrée +1, sortie −1). Calcul pur, testable sans base.
     */
    public static BigDecimal calculerSolde(Map<TypeMouvement, BigDecimal> totauxParType) {
        BigDecimal solde = MoneyUtil.ZERO;
        for (Map.Entry<TypeMouvement, BigDecimal> e : totauxParType.entrySet()) {
            solde = solde.add(e.getValue().multiply(BigDecimal.valueOf(e.getKey().getSens())));
        }
        return MoneyUtil.arrondir(solde);
    }

    /** Ma caisse (réparateur ou propriétaire ayant un profil réparateur). */
    public ResumeCaisse maCaisse(Utilisateur acteur) {
        Autorisation.exigerProfilReparateur(acteur);
        return resume(acteur, acteur.getProfilReparateur().getId());
    }

    /** Caisse d'un réparateur : le propriétaire voit tout, un réparateur seulement la sienne. */
    public ResumeCaisse resume(Utilisateur acteur, int reparateurId) {
        verifierLecture(acteur, reparateurId);
        return TransactionManager.lecture(conn -> {
            ResumeCaisse c = caisseDAO.trouverParReparateur(conn, reparateurId)
                    .orElseThrow(() -> new BusinessException("Caisse introuvable."));
            completer(conn, c);
            return c;
        });
    }

    /** Toutes les caisses (EF-17) — propriétaire uniquement. */
    public List<ResumeCaisse> toutes(Utilisateur acteur) {
        Autorisation.exigerProprietaire(acteur);
        return TransactionManager.lecture(conn -> {
            List<ResumeCaisse> liste = caisseDAO.listerToutes(conn);
            for (ResumeCaisse c : liste) {
                completer(conn, c);
            }
            return liste;
        });
    }

    public List<MouvementCaisse> mouvements(Utilisateur acteur, int reparateurId) {
        verifierLecture(acteur, reparateurId);
        return TransactionManager.lecture(conn -> {
            ResumeCaisse c = caisseDAO.trouverParReparateur(conn, reparateurId)
                    .orElseThrow(() -> new BusinessException("Caisse introuvable."));
            return caisseDAO.listerMouvements(conn, c.getCaisseId());
        });
    }

    /**
     * Mouvement manuel (EF-18) :
     * <ul>
     *   <li>ACHAT_PIECES : le réparateur sur sa propre caisse, ou le propriétaire ;</li>
     *   <li>VERSEMENT_PROPRIETAIRE, PAIEMENT_COMMISSION : propriétaire uniquement ;</li>
     *   <li>ENCAISSEMENT : interdit ici (créé automatiquement par un paiement client).</li>
     * </ul>
     * Le solde ne peut pas devenir négatif (RG-37) ; une commission payée ne peut pas dépasser
     * la commission due (RG-38).
     */
    public ResumeCaisse enregistrerSortie(Utilisateur acteur, int reparateurId, TypeMouvement type,
                                         BigDecimal montant, String libelle) {
        Autorisation.exigerConnecte(acteur);
        if (type == null || type.estEntree()) {
            throw new ValidationException("Les encaissements sont créés automatiquement par les paiements clients.");
        }
        if (type == TypeMouvement.ACHAT_PIECES) {
            verifierLecture(acteur, reparateurId);
        } else {
            Autorisation.exigerProprietaire(acteur);
        }
        if (!MoneyUtil.estStrictementPositif(montant) || montant.stripTrailingZeros().scale() > MoneyUtil.ECHELLE) {
            throw new ValidationException("Le montant doit être supérieur à 0 (2 décimales maximum).");
        }
        if (type == TypeMouvement.ACHAT_PIECES && ValidationUtil.estVide(libelle)) {
            throw new ValidationException("Indiquez quelle pièce a été achetée.");
        }
        if (ValidationUtil.depasse(libelle, 255)) {
            throw new ValidationException("Le libellé ne doit pas dépasser 255 caractères.");
        }
        BigDecimal m = MoneyUtil.arrondir(montant);
        return TransactionManager.enTransaction(conn -> {
            ResumeCaisse c = caisseDAO.trouverParReparateur(conn, reparateurId)
                    .orElseThrow(() -> new BusinessException("Caisse introuvable."));
            caisseDAO.verrouiller(conn, c.getCaisseId());
            completer(conn, c);
            if (m.compareTo(c.getSolde()) > 0) {
                throw new BusinessException("Solde insuffisant : la caisse de " + c.getNomReparateur()
                        + " contient " + MoneyUtil.formater(c.getSolde()) + ".");
            }
            if (type == TypeMouvement.PAIEMENT_COMMISSION && m.compareTo(c.getCommissionDue()) > 0) {
                throw new BusinessException("La commission due à " + c.getNomReparateur() + " est de "
                        + MoneyUtil.formater(c.getCommissionDue()) + ".");
            }
            MouvementCaisse mvt = new MouvementCaisse();
            mvt.setCaisseId(c.getCaisseId());
            mvt.setType(type);
            mvt.setMontant(m);
            mvt.setUtilisateurId(acteur.getId());
            mvt.setLibelle(ValidationUtil.estVide(libelle) ? type.getLibelle() : libelle.trim());
            caisseDAO.insererMouvement(conn, mvt);
            completer(conn, c);
            return c;
        });
    }

    private void completer(Connection conn, ResumeCaisse c) throws SQLException {
        Map<TypeMouvement, BigDecimal> totaux = caisseDAO.totauxParType(conn, c.getCaisseId());
        c.setSolde(calculerSolde(totaux));
        c.setTotalEncaisse(totaux.get(TypeMouvement.ENCAISSEMENT));
        c.setTotalAchatsPieces(totaux.get(TypeMouvement.ACHAT_PIECES));
        c.setTotalVerse(totaux.get(TypeMouvement.VERSEMENT_PROPRIETAIRE));
        c.setCommissionPayee(totaux.get(TypeMouvement.PAIEMENT_COMMISSION));
        c.setCommissionAcquise(commissionService.commissionAcquise(conn, c.getReparateurId()));
        c.setCommissionPrevisionnelle(commissionService.commissionPrevisionnelle(conn, c.getReparateurId()));
    }

    private static void verifierLecture(Utilisateur acteur, int reparateurId) {
        Autorisation.exigerConnecte(acteur);
        if (acteur.estProprietaire()) {
            return;
        }
        if (!acteur.aProfilReparateur() || acteur.getProfilReparateur().getId() != reparateurId) {
            throw new AccesRefuseException("Accès limité à votre propre caisse.");
        }
    }
}
