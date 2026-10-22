package ma.phonerepair.service;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;
import ma.phonerepair.dao.CaisseDAO;
import ma.phonerepair.dao.PaiementDAO;
import ma.phonerepair.dao.ReparationDAO;
import ma.phonerepair.dao.TransactionManager;
import ma.phonerepair.model.DossierPaiement;
import ma.phonerepair.model.MouvementCaisse;
import ma.phonerepair.model.Paiement;
import ma.phonerepair.model.Reparation;
import ma.phonerepair.model.ResumeCaisse;
import ma.phonerepair.model.SituationFinanciere;
import ma.phonerepair.model.Utilisateur;
import ma.phonerepair.model.enums.StatutPaiement;
import ma.phonerepair.model.enums.TypeMouvement;
import ma.phonerepair.model.enums.TypePaiement;
import ma.phonerepair.service.exception.BusinessException;
import ma.phonerepair.service.exception.ValidationException;
import ma.phonerepair.util.CodeGenerator;
import ma.phonerepair.util.MoneyUtil;
import ma.phonerepair.util.ValidationUtil;

/**
 * Paiements clients (RG-25 à RG-30).
 * Un paiement est rattaché à la réparation ; il entre dans la caisse du réparateur
 * qui l'encaisse (RG-36), dans la même transaction (RG-30).
 */
public class PaiementService {

    private final ReparationDAO reparationDAO = new ReparationDAO();
    private final PaiementDAO paiementDAO = new PaiementDAO();
    private final CaisseDAO caisseDAO = new CaisseDAO();
    private static final Logger LOG = Logger.getLogger(PaiementService.class.getName());

    /**
     * Calcul pur (sans base) : reste = total − payé, statut selon RG-28.
     * UNPAID si rien payé, PAID si reste = 0 et total &gt; 0, sinon PARTIALLY_PAID.
     */
    public static SituationFinanciere calculerSituation(BigDecimal total, BigDecimal paye) {
        BigDecimal t = MoneyUtil.arrondir(total);
        BigDecimal p = MoneyUtil.arrondir(paye);
        BigDecimal reste = t.subtract(p);
        StatutPaiement statut;
        if (p.signum() == 0) {
            statut = StatutPaiement.UNPAID;
        } else if (reste.signum() <= 0 && t.signum() > 0) {
            statut = StatutPaiement.PAID;
        } else {
            statut = StatutPaiement.PARTIALLY_PAID;
        }
        return new SituationFinanciere(t, p, reste, statut);
    }

    /** Situation d'une réparation, avec contrôle d'accès. */
    public SituationFinanciere situation(Utilisateur acteur, int reparationId) {
        Autorisation.exigerConnecte(acteur);
        return TransactionManager.lecture(conn -> situation(conn, reparationId));
    }

    /** Version interne, dans une connexion existante (utilisée par les autres services). */
    SituationFinanciere situation(Connection conn, int reparationId) throws SQLException {
        return calculerSituation(reparationDAO.montantTotal(conn, reparationId),
                paiementDAO.totalPaye(conn, reparationId));
    }

    /**
     * Prépare l'encaissement à partir du code de suivi. Tout réparateur peut encaisser
     * (le client peut revenir quand son réparateur est absent — RG-36).
     */
    public DossierPaiement chargerDossier(Utilisateur acteur, String code) {
        Autorisation.exigerProfilReparateur(acteur);
        String c = CodeGenerator.normaliser(code);
        if (!CodeGenerator.estFormatValide(c)) {
            throw new ValidationException("Code de suivi invalide. Format attendu : REP-2026-000123.");
        }
        return TransactionManager.lecture(conn -> {
            Reparation r = reparationDAO.trouverParCode(conn, c)
                    .orElseThrow(() -> new BusinessException("Aucune réparation avec le code " + c + "."));
            return dossier(conn, r);
        });
    }

    /**
     * Enregistre un paiement client et le mouvement ENCAISSEMENT dans la caisse de l'acteur,
     * dans une seule transaction (RG-25, RG-26, RG-30, RG-36).
     *
     * @return la nouvelle situation (total / payé / reste / statut)
     */
    public SituationFinanciere enregistrer(Utilisateur acteur, int reparationId, BigDecimal montant,
                                           TypePaiement type, String note) {
        Autorisation.exigerProfilReparateur(acteur);
        if (!MoneyUtil.estStrictementPositif(montant)) {
            throw new ValidationException("Le montant du paiement doit être supérieur à 0.");
        }
        if (montant.stripTrailingZeros().scale() > MoneyUtil.ECHELLE) {
            throw new ValidationException("Le montant accepte au maximum 2 décimales.");
        }
        if (type == null) {
            throw new ValidationException("Choisissez le mode de paiement.");
        }
        if (ValidationUtil.depasse(note, 255)) {
            throw new ValidationException("La note ne doit pas dépasser 255 caractères.");
        }
        BigDecimal m = MoneyUtil.arrondir(montant);
        return TransactionManager.enTransaction(conn -> {
            Reparation r = reparationDAO.trouverParId(conn, reparationId)
                    .orElseThrow(() -> new BusinessException("Réparation introuvable."));
            reparationDAO.verrouiller(conn, reparationId);
            SituationFinanciere avant = situation(conn, reparationId);
            if (avant.getReste().signum() <= 0) {
                throw new BusinessException(avant.getTotal().signum() == 0
                        ? "Aucun montant à payer : la réparation n'est pas encore chiffrée."
                        : "Cette réparation est déjà entièrement payée.");
            }
            if (m.compareTo(avant.getReste()) > 0) {
                throw new ValidationException("Le reste à payer est de " + MoneyUtil.formater(avant.getReste())
                        + " : le paiement ne peut pas dépasser ce montant.");
            }
            ResumeCaisse caisse = caisseDAO.trouverParReparateur(conn, acteur.getProfilReparateur().getId())
                    .orElseThrow(() -> new BusinessException("Votre caisse est introuvable."));

            Paiement p = new Paiement();
            p.setReparationId(reparationId);
            p.setMontant(m);
            p.setTypePaiement(type);
            p.setUtilisateurId(acteur.getId());
            p.setNote(ValidationUtil.estVide(note) ? null : note.trim());
            int paiementId = paiementDAO.inserer(conn, p);

            MouvementCaisse mvt = new MouvementCaisse();
            mvt.setCaisseId(caisse.getCaisseId());
            mvt.setType(TypeMouvement.ENCAISSEMENT);
            mvt.setMontant(m);
            mvt.setPaiementId(paiementId);
            mvt.setUtilisateurId(acteur.getId());
            mvt.setLibelle("Paiement " + r.getCodeSuivi() + " — " + type.getLibelle());
            caisseDAO.insererMouvement(conn, mvt);

            LOG.info("Paiement de " + m + " DH sur " + r.getCodeSuivi() + " par " + acteur.getUsername());
            return situation(conn, reparationId);
        });
    }

    private DossierPaiement dossier(Connection conn, Reparation r) throws SQLException {
        DossierPaiement d = new DossierPaiement();
        d.setReparationId(r.getId());
        d.setCodeSuivi(r.getCodeSuivi());
        d.setNomClient(r.getClient().getNomComplet());
        d.setTelephoneClient(r.getClient().getTelephone());
        d.setSituation(situation(conn, r.getId()));
        d.setPaiements(paiementDAO.listerParReparation(conn, r.getId()));
        return d;
    }
}
