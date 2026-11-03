package ma.phonerepair.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import ma.phonerepair.model.Client;

/** Accès à la table client. */
public interface ClientDAO {

    /** Recherche par début de téléphone ou par morceau de nom. */
    List<Client> rechercher(Connection conn, String texte) throws SQLException;

    List<Client> trouverParTelephone(Connection conn, String telephone) throws SQLException;

    Optional<Client> trouverParId(Connection conn, int id) throws SQLException;

    /** @return l'id généré */
    int inserer(Connection conn, Client c) throws SQLException;
}
