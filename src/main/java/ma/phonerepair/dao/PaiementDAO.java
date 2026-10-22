package ma.phonerepair.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import ma.phonerepair.model.Paiement;
import ma.phonerepair.model.enums.TypePaiement;

/** Accès à la table paiement (un versement client = une ligne, RG-25). */
public class PaiementDAO {

    public BigDecimal totalPaye(Connection conn, int reparationId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT " + ReparationDAO.sousRequetePaye("?"))) {
            ps.setInt(1, reparationId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getBigDecimal(1);
            }
        }
    }

    /** @return l'id généré */
    public int inserer(Connection conn, Paiement p) throws SQLException {
        String sql = "INSERT INTO paiement (reparation_id, montant, type_paiement, utilisateur_id, note) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, p.getReparationId());
            ps.setBigDecimal(2, p.getMontant());
            ps.setString(3, p.getTypePaiement().name());
            ps.setInt(4, p.getUtilisateurId());
            ps.setString(5, p.getNote());
            ps.executeUpdate();
            return DaoUtil.idGenere(ps);
        }
    }

    /** Historique des paiements d'une réparation, du plus ancien au plus récent. */
    public List<Paiement> listerParReparation(Connection conn, int reparationId) throws SQLException {
        String sql = "SELECT p.id, p.reparation_id, p.montant, p.date_paiement, p.type_paiement, p.utilisateur_id, "
                + "u.nom_complet, p.note FROM paiement p JOIN utilisateur u ON u.id = p.utilisateur_id "
                + "WHERE p.reparation_id = ? ORDER BY p.date_paiement, p.id";
        List<Paiement> liste = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, reparationId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Paiement p = new Paiement();
                    p.setId(rs.getInt("id"));
                    p.setReparationId(rs.getInt("reparation_id"));
                    p.setMontant(rs.getBigDecimal("montant"));
                    p.setDatePaiement(DaoUtil.dateHeure(rs, "date_paiement"));
                    p.setTypePaiement(TypePaiement.valueOf(rs.getString("type_paiement")));
                    p.setUtilisateurId(rs.getInt("utilisateur_id"));
                    p.setNomUtilisateur(rs.getString("nom_complet"));
                    p.setNote(rs.getString("note"));
                    liste.add(p);
                }
            }
        }
        return liste;
    }
}
