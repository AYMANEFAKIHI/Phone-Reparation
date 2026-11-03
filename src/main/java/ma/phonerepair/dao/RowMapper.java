package ma.phonerepair.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Transforme la ligne courante d'un ResultSet en objet Java.
 * Interface fonctionnelle : on la passe sous forme de lambda ou de référence de méthode.
 *
 * <pre>
 * RowMapper&lt;String&gt; nom = rs -&gt; rs.getString("nom");
 * </pre>
 */
@FunctionalInterface
public interface RowMapper<T> {

    T mapper(ResultSet rs) throws SQLException;
}
