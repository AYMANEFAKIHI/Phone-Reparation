package ma.phonerepair.dao.impl;

import static ma.phonerepair.dao.JdbcUtil.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import ma.phonerepair.dao.UtilisateurDAO;
import ma.phonerepair.model.Reparateur;
import ma.phonerepair.model.Utilisateur;
import ma.phonerepair.model.enums.Role;

public class UtilisateurDAOImpl implements UtilisateurDAO {

    /** LEFT JOIN : on sait tout de suite si l'utilisateur a un profil réparateur. */
    private static final String SELECT =
            "SELECT u.*, r.id AS reparateur_id, r.taux_commission "
            + "FROM utilisateur u LEFT JOIN reparateur r ON r.utilisateur_id = u.id ";

    @Override
    public Optional<Utilisateur> trouverParUsername(Connection conn, String username) throws SQLException {
        return un(conn, SELECT + "WHERE u.username = ?", this::mapper, username);
    }

    @Override
    public Optional<Utilisateur> trouverParId(Connection conn, int id) throws SQLException {
        return un(conn, SELECT + "WHERE u.id = ?", this::mapper, id);
    }

    @Override
    public List<Utilisateur> listerTous(Connection conn) throws SQLException {
        return liste(conn, SELECT + "ORDER BY u.role = 'REPARATEUR', u.nom_complet", this::mapper);
    }

    @Override
    public int inserer(Connection conn, Utilisateur u) throws SQLException {
        return ajouter(conn,
                "INSERT INTO utilisateur (nom_complet, telephone, username, password_hash, role, actif) "
                + "VALUES (?, ?, ?, ?, ?, ?)",
                u.getNomComplet(), u.getTelephone(), u.getUsername(), u.getPasswordHash(), u.getRole(), u.isActif());
    }

    @Override
    public void modifierActif(Connection conn, int id, boolean actif) throws SQLException {
        executer(conn, "UPDATE utilisateur SET actif = ? WHERE id = ?", actif, id);
    }

    @Override
    public void modifierMotDePasse(Connection conn, int id, String passwordHash) throws SQLException {
        executer(conn, "UPDATE utilisateur SET password_hash = ? WHERE id = ?", passwordHash, id);
    }

    @Override
    public void modifierInfos(Connection conn, int id, String nomComplet, String telephone) throws SQLException {
        executer(conn, "UPDATE utilisateur SET nom_complet = ?, telephone = ? WHERE id = ?", nomComplet, telephone, id);
    }

    private Utilisateur mapper(ResultSet rs) throws SQLException {
        Utilisateur u = new Utilisateur();
        u.setId(rs.getInt("id"));
        u.setNomComplet(rs.getString("nom_complet"));
        u.setTelephone(rs.getString("telephone"));
        u.setUsername(rs.getString("username"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setRole(enumeration(rs, "role", Role.class));
        u.setActif(rs.getBoolean("actif"));
        u.setDateCreation(dateHeure(rs, "date_creation"));

        Integer reparateurId = entier(rs, "reparateur_id");
        if (reparateurId != null) {
            Reparateur r = new Reparateur();
            r.setId(reparateurId);
            r.setUtilisateurId(u.getId());
            r.setTauxCommission(rs.getBigDecimal("taux_commission"));
            u.setProfilReparateur(r);
        }
        return u;
    }
}
