package ma.phonerepair.service;

import java.util.Optional;
import java.util.logging.Logger;
import ma.phonerepair.dao.TransactionManager;
import ma.phonerepair.dao.UtilisateurDAO;
import ma.phonerepair.model.Utilisateur;
import ma.phonerepair.service.exception.BusinessException;
import ma.phonerepair.service.exception.ValidationException;
import ma.phonerepair.util.PasswordUtil;
import ma.phonerepair.util.ValidationUtil;

/** Authentification des propriétaires et réparateurs (EF-01). Les clients n'ont pas de compte. */
public class AuthService {

    private static final Logger LOG = Logger.getLogger(AuthService.class.getName());
    static final String MSG_IDENTIFIANTS = "Nom d'utilisateur ou mot de passe incorrect.";

    private final UtilisateurDAO utilisateurDAO = new UtilisateurDAO();

    /**
     * Vérifie les identifiants. Le mot de passe est effacé de la mémoire après usage.
     *
     * @return l'utilisateur connecté, avec son profil réparateur s'il en a un
     */
    public Utilisateur connecter(String username, char[] motDePasse) {
        try {
            if (ValidationUtil.estVide(username) || motDePasse == null || motDePasse.length == 0) {
                throw new ValidationException("Saisissez le nom d'utilisateur et le mot de passe.");
            }
            Optional<Utilisateur> trouve = TransactionManager.lecture(
                    conn -> utilisateurDAO.trouverParUsername(conn, username.trim()));

            // Même message si l'utilisateur n'existe pas ou si le mot de passe est faux :
            // on ne révèle pas quels noms d'utilisateur existent.
            if (trouve.isEmpty() || !PasswordUtil.verifier(motDePasse, trouve.get().getPasswordHash())) {
                LOG.info("Échec de connexion pour « " + username + " »");
                throw new BusinessException(MSG_IDENTIFIANTS);
            }
            Utilisateur utilisateur = trouve.get();
            if (!utilisateur.isActif()) {
                throw new BusinessException("Compte désactivé. Contactez le propriétaire de la boutique.");
            }
            LOG.info("Connexion de " + utilisateur.getUsername());
            return utilisateur;
        } finally {
            PasswordUtil.effacer(motDePasse);
        }
    }

    /** Recharge l'utilisateur depuis la base (ex. après activation du profil réparateur). */
    public Utilisateur recharger(Utilisateur utilisateur) {
        return TransactionManager.lecture(conn -> utilisateurDAO.trouverParId(conn, utilisateur.getId()))
                .orElseThrow(() -> new BusinessException("Utilisateur introuvable."));
    }

    /** Changement de son propre mot de passe. */
    public void changerMotDePasse(Utilisateur acteur, char[] ancien, char[] nouveau) {
        try {
            Autorisation.exigerConnecte(acteur);
            Utilisateur actuel = recharger(acteur);
            if (!PasswordUtil.verifier(ancien, actuel.getPasswordHash())) {
                throw new ValidationException("L'ancien mot de passe est incorrect.");
            }
            if (!ValidationUtil.estMotDePasseValide(nouveau)) {
                throw new ValidationException("Le nouveau mot de passe doit contenir au moins "
                        + ValidationUtil.MOT_DE_PASSE_MIN + " caractères.");
            }
            String hash = PasswordUtil.hacher(nouveau);
            TransactionManager.enTransaction(conn -> {
                utilisateurDAO.modifierMotDePasse(conn, acteur.getId(), hash);
                return null;
            });
        } finally {
            PasswordUtil.effacer(ancien);
            PasswordUtil.effacer(nouveau);
        }
    }
}
