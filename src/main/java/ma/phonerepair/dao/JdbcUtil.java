package ma.phonerepair.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Le code JDBC répétitif (PreparedStatement, paramètres, boucle sur le ResultSet,
 * fermeture) écrit une seule fois. Les DAO n'écrivent que leur SQL et leur mapping.
 * <p>
 * Les paramètres sont toujours passés par des « ? » : jamais de concaténation de saisie utilisateur.
 *
 * <pre>
 * List&lt;Client&gt; l = JdbcUtil.liste(conn, "SELECT * FROM client WHERE telephone = ?", this::mapper, tel);
 * </pre>
 */
public final class JdbcUtil {

    private JdbcUtil() {
    }

    /** SELECT → liste d'objets (vide si aucune ligne). */
    public static <T> List<T> liste(Connection conn, String sql, RowMapper<T> mapper, Object... params)
            throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            remplir(ps, params);
            List<T> liste = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    liste.add(mapper.mapper(rs));
                }
            }
            return liste;
        }
    }

    /** SELECT → première ligne, ou Optional vide (aucune ligne, ou valeur NULL comme un MAX() sur table vide). */
    public static <T> Optional<T> un(Connection conn, String sql, RowMapper<T> mapper, Object... params)
            throws SQLException {
        return liste(conn, sql, mapper, params).stream().filter(Objects::nonNull).findFirst();
    }

    /** SELECT de 2 colonnes → Map (ordre des lignes conservé). */
    public static <K, V> Map<K, V> map(Connection conn, String sql, RowMapper<K> cle, RowMapper<V> valeur,
            Object... params) throws SQLException {
        Map<K, V> resultat = new LinkedHashMap<>();
        liste(conn, sql, rs -> Map.entry(cle.mapper(rs), valeur.mapper(rs)), params)
                .forEach(e -> resultat.put(e.getKey(), e.getValue()));
        return resultat;
    }

    /** SELECT d'une somme (première colonne) ; 0 si NULL. */
    public static BigDecimal montant(Connection conn, String sql, Object... params) throws SQLException {
        return un(conn, sql, rs -> rs.getBigDecimal(1), params).orElse(BigDecimal.ZERO);
    }

    /** SELECT COUNT(*) (première colonne). */
    public static int nombre(Connection conn, String sql, Object... params) throws SQLException {
        return un(conn, sql, rs -> rs.getInt(1), params).orElse(0);
    }

    /** UPDATE / DELETE / INSERT sans id à récupérer. @return nombre de lignes touchées */
    public static int executer(Connection conn, String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            remplir(ps, params);
            return ps.executeUpdate();
        }
    }

    /** INSERT dans une table AUTO_INCREMENT. @return l'id généré */
    public static int ajouter(Connection conn, String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            remplir(ps, params);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("Aucun identifiant généré");
                }
                return keys.getInt(1);
            }
        }
    }

    /**
     * Verrouille une ligne jusqu'à la fin de la transaction.
     * La requête doit être un « SELECT ... WHERE id = ? FOR UPDATE ».
     */
    public static void verrouillerLigne(Connection conn, String sqlForUpdate, int id) throws SQLException {
        liste(conn, sqlForUpdate, rs -> rs.getInt(1), id);
    }

    /** Lit une colonne DATETIME (peut être NULL). */
    public static LocalDateTime dateHeure(ResultSet rs, String colonne) throws SQLException {
        Timestamp t = rs.getTimestamp(colonne);
        return t == null ? null : t.toLocalDateTime();
    }

    /** Lit une colonne DATE (peut être NULL). */
    public static LocalDate date(ResultSet rs, String colonne) throws SQLException {
        Date d = rs.getDate(colonne);
        return d == null ? null : d.toLocalDate();
    }

    /** Lit une colonne VARCHAR contenant le nom d'une constante d'enum (peut être NULL). */
    public static <E extends Enum<E>> E enumeration(ResultSet rs, String colonne, Class<E> type) throws SQLException {
        String valeur = rs.getString(colonne);
        return valeur == null ? null : Enum.valueOf(type, valeur);
    }

    /** Lit une colonne INT pouvant être NULL. */
    public static Integer entier(ResultSet rs, String colonne) throws SQLException {
        int v = rs.getInt(colonne);
        return rs.wasNull() ? null : v;
    }

    /** Place les paramètres dans l'ordre des « ? ». Enums → nom, dates → types SQL. */
    private static void remplir(PreparedStatement ps, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            Object p = params[i];
            if (p == null) {
                ps.setNull(i + 1, Types.NULL);
            } else if (p instanceof Enum<?> e) {
                ps.setString(i + 1, e.name());
            } else if (p instanceof LocalDate d) {
                ps.setDate(i + 1, Date.valueOf(d));
            } else if (p instanceof LocalDateTime t) {
                ps.setTimestamp(i + 1, Timestamp.valueOf(t));
            } else {
                ps.setObject(i + 1, p);
            }
        }
    }
}
