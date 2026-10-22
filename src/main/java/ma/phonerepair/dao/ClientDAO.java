package ma.phonerepair.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import ma.phonerepair.model.Client;

/** Accès à la table client. */
public class ClientDAO {

    private static final String SELECT = "SELECT id, nom_complet, telephone, date_creation FROM client ";
    private static final int LIMITE_RECHERCHE = 50;

    /** Recherche par début de téléphone ou par morceau de nom. */
    public List<Client> rechercher(Connection conn, String texte) throws SQLException {
        String sql = SELECT + "WHERE telephone LIKE ? OR nom_complet LIKE ? ORDER BY nom_complet LIMIT "
                + LIMITE_RECHERCHE;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, texte + "%");
            ps.setString(2, "%" + texte + "%");
            return lireListe(ps);
        }
    }

    public List<Client> trouverParTelephone(Connection conn, String telephone) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(SELECT + "WHERE telephone = ? ORDER BY nom_complet")) {
            ps.setString(1, telephone);
            return lireListe(ps);
        }
    }

    public Optional<Client> trouverParId(Connection conn, int id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(SELECT + "WHERE id = ?")) {
            ps.setInt(1, id);
            List<Client> l = lireListe(ps);
            return l.isEmpty() ? Optional.empty() : Optional.of(l.get(0));
        }
    }

    /** @return l'id généré */
    public int inserer(Connection conn, Client c) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO client (nom_complet, telephone) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getNomComplet());
            ps.setString(2, c.getTelephone());
            ps.executeUpdate();
            return DaoUtil.idGenere(ps);
        }
    }

    private List<Client> lireListe(PreparedStatement ps) throws SQLException {
        List<Client> liste = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                liste.add(mapper(rs, "id", "nom_complet", "telephone"));
            }
        }
        return liste;
    }

    /** Réutilisé par ReparationDAO pour les jointures (colonnes aliasées). */
    static Client mapper(ResultSet rs, String colId, String colNom, String colTel) throws SQLException {
        Client c = new Client();
        c.setId(rs.getInt(colId));
        c.setNomComplet(rs.getString(colNom));
        c.setTelephone(rs.getString(colTel));
        return c;
    }
}
