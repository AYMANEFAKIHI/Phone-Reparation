package ma.phonerepair.dao.impl;

import static ma.phonerepair.dao.JdbcUtil.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import ma.phonerepair.dao.InterventionDAO;
import ma.phonerepair.model.Intervention;

public class InterventionDAOImpl implements InterventionDAO {

    private static final String SELECT = "SELECT i.* FROM intervention i ";

    @Override
    public int inserer(Connection conn, Intervention i) throws SQLException {
        return ajouter(conn, "INSERT INTO intervention (appareil_id, reparateur_id, diagnostic, pieces_description, "
                + "cout_achat_pieces, prix_pieces_client, main_oeuvre, taux_commission_applique) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                i.getAppareilId(), i.getReparateurId(), i.getDiagnostic(), i.getPiecesDescription(),
                i.getCoutAchatPieces(), i.getPrixPiecesClient(), i.getMainOeuvre(), i.getTauxCommissionApplique());
    }

    @Override
    public void modifierContenu(Connection conn, Intervention i) throws SQLException {
        executer(conn, "UPDATE intervention SET diagnostic = ?, pieces_description = ?, cout_achat_pieces = ?, "
                + "prix_pieces_client = ?, main_oeuvre = ? WHERE id = ?",
                i.getDiagnostic(), i.getPiecesDescription(), i.getCoutAchatPieces(), i.getPrixPiecesClient(),
                i.getMainOeuvre(), i.getId());
    }

    @Override
    public List<Intervention> listerParAppareil(Connection conn, int appareilId) throws SQLException {
        return liste(conn, SELECT + "WHERE i.appareil_id = ? ORDER BY i.id", InterventionDAOImpl::mapper, appareilId);
    }

    @Override
    public List<Intervention> listerParReparation(Connection conn, int reparationId) throws SQLException {
        return liste(conn, SELECT + "JOIN appareil a ON a.id = i.appareil_id WHERE a.reparation_id = ? ORDER BY i.id",
                InterventionDAOImpl::mapper, reparationId);
    }

    @Override
    public List<Intervention> listerPourCommission(Connection conn, int reparateurId, boolean livrees)
            throws SQLException {
        String condition = livrees ? "a.statut = 'DELIVERED'" : "a.statut NOT IN ('DELIVERED', 'CANCELLED')";
        return liste(conn, SELECT + "JOIN appareil a ON a.id = i.appareil_id WHERE i.reparateur_id = ? AND "
                + condition, InterventionDAOImpl::mapper, reparateurId);
    }

    /** Aussi utilisé par StatistiqueDAOImpl. */
    static Intervention mapper(ResultSet rs) throws SQLException {
        Intervention i = new Intervention();
        i.setId(rs.getInt("id"));
        i.setAppareilId(rs.getInt("appareil_id"));
        i.setReparateurId(rs.getInt("reparateur_id"));
        i.setDiagnostic(rs.getString("diagnostic"));
        i.setPiecesDescription(rs.getString("pieces_description"));
        i.setCoutAchatPieces(rs.getBigDecimal("cout_achat_pieces"));
        i.setPrixPiecesClient(rs.getBigDecimal("prix_pieces_client"));
        i.setMainOeuvre(rs.getBigDecimal("main_oeuvre"));
        i.setTauxCommissionApplique(rs.getBigDecimal("taux_commission_applique"));
        i.setDateIntervention(dateHeure(rs, "date_intervention"));
        return i;
    }
}
