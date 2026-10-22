package ma.phonerepair.dao;

import java.sql.SQLException;

/**
 * Erreur technique d'accès à la base. Enveloppe une {@link SQLException} pour que
 * les couches supérieures ne manipulent jamais d'exception JDBC brute.
 */
public class DataAccessException extends RuntimeException {

    /** SQLState de la classe 23 = violation de contrainte (UNIQUE, FK, CHECK). */
    private static final String CLASSE_CONTRAINTE = "23";

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }

    /** Vrai si l'erreur vient d'une contrainte (doublon, clé étrangère, CHECK). */
    public boolean estViolationContrainte() {
        return getCause() instanceof SQLException sql
                && sql.getSQLState() != null
                && sql.getSQLState().startsWith(CLASSE_CONTRAINTE);
    }

    /** Vrai si la contrainte violée porte le nom donné (ex. "uq_utilisateur_username"). */
    public boolean concerne(String nomContrainte) {
        return estViolationContrainte()
                && getCause().getMessage() != null
                && getCause().getMessage().contains(nomContrainte);
    }
}
