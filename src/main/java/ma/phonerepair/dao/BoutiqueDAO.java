package ma.phonerepair.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Optional;
import ma.phonerepair.model.Boutique;

/** Accès à la table boutique (une seule ligne, id = 1). */
public class BoutiqueDAO {

    private static final String SELECT =
            "SELECT id, nom, adresse, telephone, numero_patente, date_creation FROM boutique WHERE id = 1";

    private static final String INSERT =
            "INSERT INTO boutique (id, nom, adresse, telephone, numero_patente) VALUES (1, ?, ?, ?, ?)";

    private static final String UPDATE =
            "UPDATE boutique SET nom = ?, adresse = ?, telephone = ?, numero_patente = ? WHERE id = 1";

    public Optional<Boutique> trouver(Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(SELECT);
             ResultSet rs = ps.executeQuery()) {
            if (!rs.next()) {
                return Optional.empty();
            }
            Boutique b = new Boutique();
            b.setId(rs.getInt("id"));
            b.setNom(rs.getString("nom"));
            b.setAdresse(rs.getString("adresse"));
            b.setTelephone(rs.getString("telephone"));
            b.setNumeroPatente(rs.getString("numero_patente"));
            Timestamp creation = rs.getTimestamp("date_creation");
            b.setDateCreation(creation == null ? null : creation.toLocalDateTime());
            return Optional.of(b);
        }
    }

    public void inserer(Connection conn, Boutique b) throws SQLException {
        executer(conn, INSERT, b);
    }

    public void modifier(Connection conn, Boutique b) throws SQLException {
        executer(conn, UPDATE, b);
    }

    private void executer(Connection conn, String sql, Boutique b) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, b.getNom());
            ps.setString(2, b.getAdresse());
            ps.setString(3, b.getTelephone());
            ps.setString(4, b.getNumeroPatente());
            ps.executeUpdate();
        }
    }
}
