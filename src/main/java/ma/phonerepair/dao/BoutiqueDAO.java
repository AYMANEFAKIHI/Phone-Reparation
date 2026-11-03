package ma.phonerepair.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import ma.phonerepair.model.Boutique;

/** Accès à la table boutique (une seule ligne, id = 1). */
public interface BoutiqueDAO {

    Optional<Boutique> trouver(Connection conn) throws SQLException;

    void inserer(Connection conn, Boutique b) throws SQLException;

    void modifier(Connection conn, Boutique b) throws SQLException;
}
