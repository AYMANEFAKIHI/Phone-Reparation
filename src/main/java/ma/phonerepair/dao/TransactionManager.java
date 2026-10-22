package ma.phonerepair.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import ma.phonerepair.util.DatabaseConnection;

/**
 * Ouvre une connexion, exécute un travail, puis commit ou rollback.
 * <p>
 * Utilisé par les services : c'est le service qui décide de ce qui forme une
 * transaction ; les DAO reçoivent la {@link Connection} en paramètre.
 *
 * <pre>
 * int id = TransactionManager.enTransaction(conn -&gt; {
 *     int idUser = utilisateurDAO.inserer(conn, u);
 *     reparateurDAO.inserer(conn, idUser, taux);
 *     return idUser;
 * });
 * </pre>
 */
public final class TransactionManager {

    private static final Logger LOG = Logger.getLogger(TransactionManager.class.getName());

    /** Travail SQL qui peut lever une SQLException. */
    @FunctionalInterface
    public interface Travail<T> {
        T executer(Connection conn) throws SQLException;
    }

    private TransactionManager() {
    }

    /** Exécute plusieurs requêtes de façon atomique : tout ou rien. */
    public static <T> T enTransaction(Travail<T> travail) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                T resultat = travail.executer(conn);
                conn.commit();
                return resultat;
            } catch (SQLException | RuntimeException e) {
                annuler(conn);
                throw e;
            }
        } catch (SQLException e) {
            throw traduire(e);
        }
    }

    /** Exécute une lecture simple (auto-commit). */
    public static <T> T lecture(Travail<T> travail) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return travail.executer(conn);
        } catch (SQLException e) {
            throw traduire(e);
        }
    }

    private static void annuler(Connection conn) {
        try {
            conn.rollback();
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Échec du rollback", e);
        }
    }

    private static DataAccessException traduire(SQLException e) {
        DataAccessException dae = new DataAccessException("Erreur d'accès à la base de données", e);
        // Les violations de contrainte sont des erreurs « attendues » : le service les traduit.
        LOG.log(dae.estViolationContrainte() ? Level.FINE : Level.SEVERE,
                "Erreur SQL (state " + e.getSQLState() + ")", e);
        return dae;
    }
}
