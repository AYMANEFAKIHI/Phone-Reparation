package ma.phonerepair.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

/** Accès à la table reparateur (profil réparateur d'un utilisateur). */
public class ReparateurDAO {

    /** @return l'id du profil créé */
    public int inserer(Connection conn, int utilisateurId, BigDecimal tauxCommission) throws SQLException {
        String sql = "INSERT INTO reparateur (utilisateur_id, taux_commission) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, utilisateurId);
            ps.setBigDecimal(2, tauxCommission);
            ps.executeUpdate();
            return DaoUtil.idGenere(ps);
        }
    }

    /** Modifie le taux actuel. N'affecte pas les interventions existantes (RG-33). */
    public void modifierTaux(Connection conn, int reparateurId, BigDecimal tauxCommission) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE reparateur SET taux_commission = ? WHERE id = ?")) {
            ps.setBigDecimal(1, tauxCommission);
            ps.setInt(2, reparateurId);
            ps.executeUpdate();
        }
    }

    /** Taux actuel d'un réparateur, à copier dans une nouvelle intervention. */
    public Optional<BigDecimal> trouverTaux(Connection conn, int reparateurId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT taux_commission FROM reparateur WHERE id = ?")) {
            ps.setInt(1, reparateurId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(rs.getBigDecimal(1)) : Optional.empty();
            }
        }
    }
}
