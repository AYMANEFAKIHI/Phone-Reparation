package ma.phonerepair.dao.impl;

import static ma.phonerepair.dao.JdbcUtil.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import ma.phonerepair.dao.ClientDAO;
import ma.phonerepair.model.Client;

public class ClientDAOImpl implements ClientDAO {

    private static final String SELECT = "SELECT id, nom_complet, telephone FROM client ";

    @Override
    public List<Client> rechercher(Connection conn, String texte) throws SQLException {
        return liste(conn, SELECT + "WHERE telephone LIKE ? OR nom_complet LIKE ? ORDER BY nom_complet LIMIT 50",
                rs -> mapper(rs, "id", "nom_complet", "telephone"), texte + "%", "%" + texte + "%");
    }

    @Override
    public List<Client> trouverParTelephone(Connection conn, String telephone) throws SQLException {
        return liste(conn, SELECT + "WHERE telephone = ? ORDER BY nom_complet",
                rs -> mapper(rs, "id", "nom_complet", "telephone"), telephone);
    }

    @Override
    public Optional<Client> trouverParId(Connection conn, int id) throws SQLException {
        return un(conn, SELECT + "WHERE id = ?", rs -> mapper(rs, "id", "nom_complet", "telephone"), id);
    }

    @Override
    public int inserer(Connection conn, Client c) throws SQLException {
        return ajouter(conn, "INSERT INTO client (nom_complet, telephone) VALUES (?, ?)",
                c.getNomComplet(), c.getTelephone());
    }

    /** Réutilisé par ReparationDAOImpl pour les jointures (colonnes aliasées). */
    static Client mapper(ResultSet rs, String colId, String colNom, String colTel) throws SQLException {
        Client c = new Client();
        c.setId(rs.getInt(colId));
        c.setNomComplet(rs.getString(colNom));
        c.setTelephone(rs.getString(colTel));
        return c;
    }
}
