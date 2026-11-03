package ma.phonerepair.dao.impl;

import static ma.phonerepair.dao.JdbcUtil.*;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import ma.phonerepair.dao.ReparateurDAO;

public class ReparateurDAOImpl implements ReparateurDAO {

    @Override
    public int inserer(Connection conn, int utilisateurId, BigDecimal tauxCommission) throws SQLException {
        return ajouter(conn, "INSERT INTO reparateur (utilisateur_id, taux_commission) VALUES (?, ?)",
                utilisateurId, tauxCommission);
    }

    @Override
    public void modifierTaux(Connection conn, int reparateurId, BigDecimal tauxCommission) throws SQLException {
        executer(conn, "UPDATE reparateur SET taux_commission = ? WHERE id = ?", tauxCommission, reparateurId);
    }

    @Override
    public Optional<BigDecimal> trouverTaux(Connection conn, int reparateurId) throws SQLException {
        return un(conn, "SELECT taux_commission FROM reparateur WHERE id = ?", rs -> rs.getBigDecimal(1), reparateurId);
    }
}
