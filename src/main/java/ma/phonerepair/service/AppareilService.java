package ma.phonerepair.service;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import ma.phonerepair.dao.AppareilDAO;
import ma.phonerepair.dao.HistoriqueStatutDAO;
import ma.phonerepair.dao.InterventionDAO;
import ma.phonerepair.dao.PaiementDAO;
import ma.phonerepair.dao.ReparateurDAO;
import ma.phonerepair.dao.ReparationDAO;
import ma.phonerepair.dao.TransactionManager;
import ma.phonerepair.model.Appareil;
import ma.phonerepair.model.HistoriqueStatut;
import ma.phonerepair.model.Intervention;
import ma.phonerepair.model.Reparation;
import ma.phonerepair.model.Utilisateur;
import ma.phonerepair.model.enums.StatutReparation;
import ma.phonerepair.service.exception.BusinessException;
import ma.phonerepair.service.exception.ValidationException;
import ma.phonerepair.util.MoneyUtil;
import ma.phonerepair.util.ValidationUtil;

/**
 * Travail sur un appareil : intervention (diagnostic, pièces, main d'œuvre) et statut.
 * Règles : RG-15 à RG-18, RG-20 à RG-24, RG-33.
 */
public class AppareilService {

    private final AppareilDAO appareilDAO = new AppareilDAO();
    private final ReparationDAO reparationDAO = new ReparationDAO();
    private final InterventionDAO interventionDAO = new InterventionDAO();
    private final HistoriqueStatutDAO historiqueDAO = new HistoriqueStatutDAO();
    private final ReparateurDAO reparateurDAO = new ReparateurDAO();
    private final PaiementDAO paiementDAO = new PaiementDAO();

    /**
     * Enregistre l'intervention d'un appareil.
     * <ul>
     *   <li>Première fois : création, avec copie du taux actuel du réparateur responsable (RG-33).</li>
     *   <li>Ensuite : modification du contenu ; le taux figé n'est jamais recopié.</li>
     * </ul>
     */
    public Intervention enregistrerIntervention(Utilisateur acteur, int appareilId, Intervention saisie) {
        Autorisation.exigerConnecte(acteur);
        Intervention i = validerIntervention(saisie);
        return TransactionManager.enTransaction(conn -> {
            Appareil a = chargerAppareilModifiable(conn, acteur, appareilId);
            Reparation r = reparationDAO.trouverParId(conn, a.getReparationId()).orElseThrow();
            List<Intervention> existantes = interventionDAO.listerParAppareil(conn, appareilId);
            i.setAppareilId(appareilId);
            if (existantes.isEmpty()) {
                i.setReparateurId(r.getReparateurId());
                i.setTauxCommissionApplique(reparateurDAO.trouverTaux(conn, r.getReparateurId())
                        .orElseThrow(() -> new BusinessException("Réparateur responsable introuvable.")));
                i.setId(interventionDAO.inserer(conn, i));
            } else {
                Intervention actuelle = existantes.get(existantes.size() - 1);
                i.setId(actuelle.getId());
                i.setReparateurId(actuelle.getReparateurId());
                i.setTauxCommissionApplique(actuelle.getTauxCommissionApplique());
                interventionDAO.modifierContenu(conn, i);
            }
            // Le nouveau total ne peut pas descendre sous ce que le client a déjà payé.
            BigDecimal total = reparationDAO.montantTotal(conn, r.getId());
            BigDecimal paye = paiementDAO.totalPaye(conn, r.getId());
            if (total.compareTo(paye) < 0) {
                throw new BusinessException("Le client a déjà payé " + MoneyUtil.formater(paye)
                        + " : le total de la réparation ne peut pas être inférieur.");
            }
            return i;
        });
    }

    /** Change le statut d'un appareil et l'historise, dans une transaction (RG-15, RG-16). */
    public void changerStatut(Utilisateur acteur, int appareilId, StatutReparation nouveau, String commentaire) {
        Autorisation.exigerConnecte(acteur);
        if (nouveau == null) {
            throw new ValidationException("Choisissez le nouveau statut.");
        }
        if (ValidationUtil.depasse(commentaire, 255)) {
            throw new ValidationException("Le commentaire ne doit pas dépasser 255 caractères.");
        }
        TransactionManager.enTransaction(conn -> {
            Appareil a = chargerAppareilModifiable(conn, acteur, appareilId);
            StatutReparation ancien = a.getStatut();
            if (!ancien.peutPasserA(nouveau)) {
                throw new BusinessException("Transition de statut non autorisée : "
                        + ancien.getLibelle() + " → " + nouveau.getLibelle() + ".");
            }
            if (nouveau == StatutReparation.DELIVERED) {
                verifierPaiementAvantLivraison(conn, a);
            }
            if (nouveau == StatutReparation.CANCELLED) {
                verifierAnnulationPossible(conn, a);
            }
            appareilDAO.modifierStatut(conn, appareilId, nouveau);

            HistoriqueStatut h = new HistoriqueStatut();
            h.setAppareilId(appareilId);
            h.setAncienStatut(ancien);
            h.setNouveauStatut(nouveau);
            h.setUtilisateurId(acteur.getId());
            h.setCommentaire(ValidationUtil.estVide(commentaire) ? null : commentaire.trim());
            historiqueDAO.inserer(conn, h);
            return null;
        });
    }

    public List<HistoriqueStatut> historique(Utilisateur acteur, int appareilId) {
        Autorisation.exigerConnecte(acteur);
        return TransactionManager.lecture(conn -> {
            Appareil a = appareilDAO.trouverParId(conn, appareilId)
                    .orElseThrow(() -> new BusinessException("Appareil introuvable."));
            ReparationService.verifierAcces(acteur, reparationDAO.trouverParId(conn, a.getReparationId()).orElseThrow());
            return historiqueDAO.listerParAppareil(conn, appareilId);
        });
    }

    /**
     * RG-18 (Q5) : un appareil ne sort que payé. Le total payé doit couvrir les appareils
     * déjà livrés + celui-ci.
     */
    private void verifierPaiementAvantLivraison(Connection conn, Appareil a) throws SQLException {
        BigDecimal aCouvrir = appareilDAO.montantLivreOuALivrer(conn, a.getReparationId(), a.getId());
        BigDecimal paye = paiementDAO.totalPaye(conn, a.getReparationId());
        if (paye.compareTo(aCouvrir) < 0) {
            throw new BusinessException("Paiement requis avant livraison : il manque "
                    + MoneyUtil.formater(aCouvrir.subtract(paye)) + ".");
        }
    }

    /** Annuler un appareil retire son montant du total : impossible si le client a déjà payé plus. */
    private void verifierAnnulationPossible(Connection conn, Appareil a) throws SQLException {
        BigDecimal montantAppareil = interventionDAO.listerParAppareil(conn, a.getId()).stream()
                .map(Intervention::getMontantClient).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalApres = reparationDAO.montantTotal(conn, a.getReparationId()).subtract(montantAppareil);
        BigDecimal paye = paiementDAO.totalPaye(conn, a.getReparationId());
        if (paye.compareTo(totalApres) > 0) {
            throw new BusinessException("Impossible d'annuler : le client a déjà payé " + MoneyUtil.formater(paye)
                    + ", le total deviendrait " + MoneyUtil.formater(totalApres)
                    + ". Le remboursement n'est pas géré par l'application.");
        }
    }

    /** Charge l'appareil, vérifie les droits et qu'il n'est ni livré ni annulé (RG-17). */
    private Appareil chargerAppareilModifiable(Connection conn, Utilisateur acteur, int appareilId)
            throws SQLException {
        Appareil a = appareilDAO.trouverParId(conn, appareilId)
                .orElseThrow(() -> new BusinessException("Appareil introuvable."));
        Reparation r = reparationDAO.trouverParId(conn, a.getReparationId()).orElseThrow();
        ReparationService.verifierAcces(acteur, r);
        if (a.getStatut().estFinal()) {
            throw new BusinessException("Cet appareil est " + a.getStatut().getLibelle().toLowerCase()
                    + " : il ne peut plus être modifié.");
        }
        return a;
    }

    static Intervention validerIntervention(Intervention s) {
        if (s == null) {
            throw new ValidationException("Intervention vide.");
        }
        BoutiqueService.exigerTexte(s.getDiagnostic(), "Le diagnostic", 500);
        if (ValidationUtil.depasse(s.getPiecesDescription(), 500)) {
            throw new ValidationException("La description des pièces ne doit pas dépasser 500 caractères.");
        }
        Intervention i = new Intervention();
        i.setDiagnostic(s.getDiagnostic().trim());
        i.setPiecesDescription(ValidationUtil.estVide(s.getPiecesDescription()) ? null : s.getPiecesDescription().trim());
        i.setCoutAchatPieces(montantPositif(s.getCoutAchatPieces(), "Le coût d'achat des pièces"));
        i.setPrixPiecesClient(montantPositif(s.getPrixPiecesClient(), "Le prix des pièces"));
        i.setMainOeuvre(montantPositif(s.getMainOeuvre(), "La main d'œuvre"));
        return i;
    }

    private static BigDecimal montantPositif(BigDecimal montant, String libelle) {
        if (montant == null) {
            return MoneyUtil.ZERO;
        }
        if (MoneyUtil.estNegatif(montant)) {
            throw new ValidationException(libelle + " ne peut pas être négatif.");
        }
        if (montant.compareTo(new BigDecimal("99999999.99")) > 0) {
            throw new ValidationException(libelle + " est trop élevé.");
        }
        return MoneyUtil.arrondir(montant);
    }
}
