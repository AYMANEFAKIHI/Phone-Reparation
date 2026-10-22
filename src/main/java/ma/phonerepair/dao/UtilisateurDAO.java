package ma.phonerepair.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import ma.phonerepair.model.Reparateur;
import ma.phonerepair.model.Utilisateur;
import ma.phonerepair.model.enums.Role;

/**
 * Accès à la table utilisateur. Chaque lecture charge aussi le profil réparateur
 * éventuel (LEFT JOIN), pour savoir immédiatement si l'utilisateur peut réparer.
 */
public class UtilisateurDAO {

    private static final String SELECT_AVEC_PROFIL =
            "SELECT u.id, u.nom_complet, u.telephone, u.username, u.password_hash, u.role, u.actif, "
            + "u.date_creation, r.id AS reparateur_id, r.taux_commission "
            + "FROM utilisateur u LEFT JOIN reparateur r ON r.utilisateur_id = u.id ";

    public Optional<Utilisateur> trouverParUsername(Connection conn, String username) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(SELECT_AVEC_PROFIL + "WHERE u.username = ?")) {
            ps.setString(1, username);
            return lireUn(ps);
        }
    }

    public Optional<Utilisateur> trouverParId(Connection conn, int id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(SELECT_AVEC_PROFIL + "WHERE u.id = ?")) {
            ps.setInt(1, id);
            return lireUn(ps);
        }
    }

    /** Tous les utilisateurs : propriétaire en premier, puis par nom. */
    public List<Utilisateur> listerTous(Connection conn) throws SQLException {
        String sql = SELECT_AVEC_PROFIL + "ORDER BY u.role = 'REPARATEUR', u.nom_complet";
        List<Utilisateur> liste = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                liste.add(mapper(rs));
            }
        }
        return liste;
    }

    /** @return l'id généré */
    public int inserer(Connection conn, Utilisateur u) throws SQLException {
        String sql = "INSERT INTO utilisateur (nom_complet, telephone, username, password_hash, role, actif) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, u.getNomComplet());
            ps.setString(2, u.getTelephone());
            ps.setString(3, u.getUsername());
            ps.setString(4, u.getPasswordHash());
            ps.setString(5, u.getRole().name());
            ps.setBoolean(6, u.isActif());
            ps.executeUpdate();
            return DaoUtil.idGenere(ps);
        }
    }

    public void modifierActif(Connection conn, int id, boolean actif) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("UPDATE utilisateur SET actif = ? WHERE id = ?")) {
            ps.setBoolean(1, actif);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public void modifierMotDePasse(Connection conn, int id, String passwordHash) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("UPDATE utilisateur SET password_hash = ? WHERE id = ?")) {
            ps.setString(1, passwordHash);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public void modifierInfos(Connection conn, int id, String nomComplet, String telephone) throws SQLException {
        String sql = "UPDATE utilisateur SET nom_complet = ?, telephone = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nomComplet);
            ps.setString(2, telephone);
            ps.setInt(3, id);
            ps.executeUpdate();
        }
    }

    private Optional<Utilisateur> lireUn(PreparedStatement ps) throws SQLException {
        try (ResultSet rs = ps.executeQuery()) {
            return rs.next() ? Optional.of(mapper(rs)) : Optional.empty();
        }
    }

    private Utilisateur mapper(ResultSet rs) throws SQLException {
        Utilisateur u = new Utilisateur();
        u.setId(rs.getInt("id"));
        u.setNomComplet(rs.getString("nom_complet"));
        u.setTelephone(rs.getString("telephone"));
        u.setUsername(rs.getString("username"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setRole(Role.valueOf(rs.getString("role")));
        u.setActif(rs.getBoolean("actif"));
        Timestamp creation = rs.getTimestamp("date_creation");
        u.setDateCreation(creation == null ? null : creation.toLocalDateTime());

        int reparateurId = rs.getInt("reparateur_id");
        if (!rs.wasNull()) {
            Reparateur r = new Reparateur();
            r.setId(reparateurId);
            r.setUtilisateurId(u.getId());
            r.setTauxCommission(rs.getBigDecimal("taux_commission"));
            u.setProfilReparateur(r);
        }
        return u;
    }
}
