package ma.phonerepair.service;

import java.math.BigDecimal;
import java.util.List;
import ma.phonerepair.dao.CaisseDAO;
import ma.phonerepair.dao.DataAccessException;
import ma.phonerepair.dao.ReparateurDAO;
import ma.phonerepair.dao.TransactionManager;
import ma.phonerepair.dao.UtilisateurDAO;
import ma.phonerepair.model.Utilisateur;
import ma.phonerepair.model.enums.Role;
import ma.phonerepair.service.exception.BusinessException;
import ma.phonerepair.service.exception.ValidationException;
import ma.phonerepair.util.PasswordUtil;
import ma.phonerepair.util.ValidationUtil;

/**
 * Gestion des comptes réparateurs par le propriétaire (EF-04, EF-05, EF-06).
 * Créer un réparateur = utilisateur + profil + caisse, dans une seule transaction.
 */
public class ReparateurService {

    private static final BigDecimal TAUX_MAX = new BigDecimal("100");

    private final UtilisateurDAO utilisateurDAO = new UtilisateurDAO();
    private final ReparateurDAO reparateurDAO = new ReparateurDAO();
    private final CaisseDAO caisseDAO = new CaisseDAO();

    /** Tous les comptes (propriétaire inclus), avec leur profil réparateur éventuel. */
    public List<Utilisateur> listerComptes(Utilisateur acteur) {
        Autorisation.exigerProprietaire(acteur);
        return TransactionManager.lecture(utilisateurDAO::listerTous);
    }

    /**
     * Crée un compte réparateur avec sa caisse.
     *
     * @return l'id du nouvel utilisateur
     */
    public int creerReparateur(Utilisateur acteur, String nomComplet, String telephone, String username,
                               char[] motDePasse, BigDecimal tauxCommission) {
        try {
            Autorisation.exigerProprietaire(acteur);
            BoutiqueService.exigerTexte(nomComplet, "Le nom complet", 100);
            String tel = validerTelephoneFacultatif(telephone);
            if (!ValidationUtil.estUsernameValide(username == null ? null : username.trim())) {
                throw new ValidationException("Nom d'utilisateur invalide : 3 à 50 caractères "
                        + "(lettres, chiffres, point, tiret, souligné).");
            }
            if (!ValidationUtil.estMotDePasseValide(motDePasse)) {
                throw new ValidationException("Le mot de passe doit contenir au moins "
                        + ValidationUtil.MOT_DE_PASSE_MIN + " caractères.");
            }
            BigDecimal taux = validerTaux(tauxCommission);

            Utilisateur u = new Utilisateur();
            u.setNomComplet(nomComplet.trim());
            u.setTelephone(tel);
            u.setUsername(username.trim());
            u.setPasswordHash(PasswordUtil.hacher(motDePasse));
            u.setRole(Role.REPARATEUR);
            u.setActif(true);

            return TransactionManager.enTransaction(conn -> {
                int utilisateurId = utilisateurDAO.inserer(conn, u);
                int reparateurId = reparateurDAO.inserer(conn, utilisateurId, taux);
                caisseDAO.creer(conn, reparateurId);
                return utilisateurId;
            });
        } catch (DataAccessException e) {
            if (e.concerne("uq_utilisateur_username")) {
                throw new ValidationException("Le nom d'utilisateur « " + username.trim() + " » est déjà utilisé.");
            }
            throw e;
        } finally {
            PasswordUtil.effacer(motDePasse);
        }
    }

    /**
     * Change le taux de commission actuel. Les interventions déjà créées gardent
     * leur taux figé (RG-33).
     */
    public void modifierTaux(Utilisateur acteur, Utilisateur cible, BigDecimal nouveauTaux) {
        Autorisation.exigerProprietaire(acteur);
        if (cible == null || !cible.aProfilReparateur()) {
            throw new BusinessException("Cet utilisateur n'a pas de profil réparateur.");
        }
        BigDecimal taux = validerTaux(nouveauTaux);
        TransactionManager.enTransaction(conn -> {
            reparateurDAO.modifierTaux(conn, cible.getProfilReparateur().getId(), taux);
            return null;
        });
    }

    /** Active ou désactive un compte réparateur (RG-03). Aucune suppression physique. */
    public void changerActif(Utilisateur acteur, Utilisateur cible, boolean actif) {
        Autorisation.exigerProprietaire(acteur);
        if (cible == null) {
            throw new BusinessException("Aucun compte sélectionné.");
        }
        if (cible.estProprietaire()) {
            throw new BusinessException("Le compte du propriétaire ne peut pas être désactivé.");
        }
        TransactionManager.enTransaction(conn -> {
            utilisateurDAO.modifierActif(conn, cible.getId(), actif);
            return null;
        });
    }

    public void reinitialiserMotDePasse(Utilisateur acteur, Utilisateur cible, char[] nouveau) {
        try {
            Autorisation.exigerProprietaire(acteur);
            if (cible == null) {
                throw new BusinessException("Aucun compte sélectionné.");
            }
            if (!ValidationUtil.estMotDePasseValide(nouveau)) {
                throw new ValidationException("Le mot de passe doit contenir au moins "
                        + ValidationUtil.MOT_DE_PASSE_MIN + " caractères.");
            }
            String hash = PasswordUtil.hacher(nouveau);
            TransactionManager.enTransaction(conn -> {
                utilisateurDAO.modifierMotDePasse(conn, cible.getId(), hash);
                return null;
            });
        } finally {
            PasswordUtil.effacer(nouveau);
        }
    }

    /**
     * Le propriétaire répare aussi (RG-02) : on lui crée un profil réparateur et une caisse,
     * sans créer de deuxième compte.
     *
     * @return le propriétaire rechargé, avec son nouveau profil
     */
    public Utilisateur activerProfilProprietaire(Utilisateur acteur, BigDecimal tauxCommission) {
        Autorisation.exigerProprietaire(acteur);
        if (acteur.aProfilReparateur()) {
            throw new BusinessException("Votre profil réparateur est déjà activé.");
        }
        BigDecimal taux = validerTaux(tauxCommission);
        return TransactionManager.enTransaction(conn -> {
            int reparateurId = reparateurDAO.inserer(conn, acteur.getId(), taux);
            caisseDAO.creer(conn, reparateurId);
            return utilisateurDAO.trouverParId(conn, acteur.getId()).orElseThrow();
        });
    }

    /** Taux entre 0 et 100, 2 décimales maximum. */
    static BigDecimal validerTaux(BigDecimal taux) {
        if (taux == null) {
            throw new ValidationException("Le taux de commission est obligatoire.");
        }
        if (taux.signum() < 0 || taux.compareTo(TAUX_MAX) > 0) {
            throw new ValidationException("Le taux de commission doit être compris entre 0 et 100 %.");
        }
        if (taux.stripTrailingZeros().scale() > 2) {
            throw new ValidationException("Le taux de commission accepte au maximum 2 décimales.");
        }
        return taux.setScale(2);
    }

    private static String validerTelephoneFacultatif(String telephone) {
        if (ValidationUtil.estVide(telephone)) {
            return null;
        }
        if (!ValidationUtil.estTelephoneValide(telephone)) {
            throw new ValidationException("Téléphone invalide (ex. 0612345678).");
        }
        return ValidationUtil.normaliserTelephone(telephone);
    }
}
