package ma.phonerepair.service;

import java.util.logging.Logger;
import ma.phonerepair.model.Utilisateur;
import ma.phonerepair.service.exception.AccesRefuseException;

/**
 * Contrôles de droits appelés au début des méthodes de service (RG-05, matrice § 11).
 * Masquer un bouton dans l'UI ne suffit pas : le service vérifie toujours.
 */
public final class Autorisation {

    private static final Logger LOG = Logger.getLogger(Autorisation.class.getName());

    private Autorisation() {
    }

    public static void exigerConnecte(Utilisateur acteur) {
        if (acteur == null || !acteur.isActif()) {
            refuser(acteur, "Vous devez être connecté pour effectuer cette action.");
        }
    }

    public static void exigerProprietaire(Utilisateur acteur) {
        exigerConnecte(acteur);
        if (!acteur.estProprietaire()) {
            refuser(acteur, "Action réservée au propriétaire.");
        }
    }

    /** Réparateur, ou propriétaire ayant activé son profil réparateur. */
    public static void exigerProfilReparateur(Utilisateur acteur) {
        exigerConnecte(acteur);
        if (!acteur.aProfilReparateur()) {
            refuser(acteur, "Action réservée aux utilisateurs ayant un profil réparateur.");
        }
    }

    private static void refuser(Utilisateur acteur, String message) {
        LOG.warning("Accès refusé pour " + (acteur == null ? "anonyme" : acteur.getUsername()) + " : " + message);
        throw new AccesRefuseException(message);
    }
}
