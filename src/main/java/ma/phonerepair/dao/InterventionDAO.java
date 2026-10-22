package ma.phonerepair.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import ma.phonerepair.model.Intervention;

/** Accès à la table intervention (diagnostic, pièces, main d'œuvre, taux figé). */
public class InterventionDAO {

    private static final String SELECT = "SELECT i.id, i.appareil_id, i.reparateur_id, i.diagnostic, "
            + "i.pieces_description, i.cout_achat_pieces, i.prix_pieces_client, i.main_oeuvre, "
            + "i.taux_commission_applique, i.date_intervention FROM intervention i ";

    /** Le taux appliqué est écrit ici une fois pour toutes (RG-33). @return l'id généré */
    public int inserer(Connection conn, Intervention i) throws SQLException {
        String sql = "INSERT INTO intervention (appareil_id, reparateur_id, diagnostic, pieces_description, "
                + "cout_achat_pieces, prix_pieces_client, main_oeuvre, taux_commission_applique) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, i.getAppareilId());
            ps.setInt(2, i.getReparateurId());
            ps.setString(3, i.getDiagnostic());
            ps.setString(4, i.getPiecesDescription());
            ps.setBigDecimal(5, i.getCoutAchatPieces());
            ps.setBigDecimal(6, i.getPrixPiecesClient());
            ps.setBigDecimal(7, i.getMainOeuvre());
            ps.setBigDecimal(8, i.getTauxCommissionApplique());
            ps.executeUpdate();
            return DaoUtil.idGenere(ps);
        }
    }

    /** Modifie le contenu de l'intervention. Le taux de commission n'est volontairement PAS modifiable. */
    public void modifierContenu(Connection conn, Intervention i) throws SQLException {
        String sql = "UPDATE intervention SET diagnostic = ?, pieces_description = ?, cout_achat_pieces = ?, "
                + "prix_pieces_client = ?, main_oeuvre = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, i.getDiagnostic());
            ps.setString(2, i.getPiecesDescription());
            ps.setBigDecimal(3, i.getCoutAchatPieces());
            ps.setBigDecimal(4, i.getPrixPiecesClient());
            ps.setBigDecimal(5, i.getMainOeuvre());
            ps.setInt(6, i.getId());
            ps.executeUpdate();
        }
    }

    public List<Intervention> listerParAppareil(Connection conn, int appareilId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(SELECT + "WHERE i.appareil_id = ? ORDER BY i.id")) {
            ps.setInt(1, appareilId);
            return lireListe(ps);
        }
    }

    /** Toutes les interventions d'une réparation en une seule requête (évite une requête par appareil). */
    public List<Intervention> listerParReparation(Connection conn, int reparationId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(SELECT
                + "JOIN appareil a ON a.id = i.appareil_id WHERE a.reparation_id = ? ORDER BY i.id")) {
            ps.setInt(1, reparationId);
            return lireListe(ps);
        }
    }

    private List<Intervention> lireListe(PreparedStatement ps) throws SQLException {
        List<Intervention> liste = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
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
                i.setDateIntervention(DaoUtil.dateHeure(rs, "date_intervention"));
                liste.add(i);
            }
        }
        return liste;
    }

    /**
     * Interventions d'un réparateur selon l'état de l'appareil, pour le calcul des commissions.
     *
     * @param livrees true = appareils DELIVERED (commission acquise, RG-35) ;
     *                false = appareils ni livrés ni annulés (commission prévisionnelle)
     */
    public List<Intervention> listerPourCommission(Connection conn, int reparateurId, boolean livrees)
            throws SQLException {
        String condition = livrees ? "a.statut = 'DELIVERED'" : "a.statut NOT IN ('DELIVERED', 'CANCELLED')";
        try (PreparedStatement ps = conn.prepareStatement(SELECT
                + "JOIN appareil a ON a.id = i.appareil_id WHERE i.reparateur_id = ? AND " + condition)) {
            ps.setInt(1, reparateurId);
            return lireListe(ps);
        }
    }
}
