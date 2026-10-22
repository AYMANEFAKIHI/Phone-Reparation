package ma.phonerepair.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Fournit des connexions JDBC. Une nouvelle connexion est ouverte à chaque appel :
 * l'appelant doit la fermer (try-with-resources).
 * <p>
 * Pas de pool de connexions : inutile pour une application desktop mono-poste.
 */
public final class DatabaseConnection {

    private static DatabaseConfig config;

    private DatabaseConnection() {
    }

    /** Permet de remplacer la configuration (tests). */
    public static synchronized void configurer(DatabaseConfig nouvelleConfig) {
        config = nouvelleConfig;
    }

    private static synchronized DatabaseConfig config() {
        if (config == null) {
            config = DatabaseConfig.charger();
        }
        return config;
    }

    public static Connection getConnection() throws SQLException {
        DatabaseConfig c = config();
        return DriverManager.getConnection(c.getUrl(), c.getUtilisateur(), c.getMotDePasse());
    }

    /** Vérifie au démarrage que la base est joignable. */
    public static void testerConnexion() throws SQLException {
        try (Connection conn = getConnection()) {
            if (!conn.isValid(3)) {
                throw new SQLException("Connexion à la base invalide");
            }
        }
    }
}
