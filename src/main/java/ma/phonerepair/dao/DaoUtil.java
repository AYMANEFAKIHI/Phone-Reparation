package ma.phonerepair.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

/** Petites aides JDBC partagées par les DAO. */
final class DaoUtil {

    private DaoUtil() {
    }

    /** Récupère l'id AUTO_INCREMENT généré par un INSERT (RETURN_GENERATED_KEYS). */
    static int idGenere(PreparedStatement ps) throws SQLException {
        try (ResultSet keys = ps.getGeneratedKeys()) {
            if (!keys.next()) {
                throw new SQLException("Aucun identifiant généré");
            }
            return keys.getInt(1);
        }
    }

    /** Lit une colonne DATETIME (peut être NULL). */
    static LocalDateTime dateHeure(ResultSet rs, String colonne) throws SQLException {
        Timestamp t = rs.getTimestamp(colonne);
        return t == null ? null : t.toLocalDateTime();
    }
}
