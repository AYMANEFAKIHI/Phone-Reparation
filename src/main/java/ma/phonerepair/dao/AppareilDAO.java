package ma.phonerepair.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import ma.phonerepair.model.Appareil;
import ma.phonerepair.model.enums.StatutReparation;

/** Accès à la table appareil. */
public class AppareilDAO {

    private static final String SELECT = "SELECT id, reparation_id, marque, modele, imei_serie, description, "
            + "etat_physique, probleme_signale, statut, date_maj_statut FROM appareil ";

    /** @return l'id généré */
    public int inserer(Connection conn, Appareil a) throws SQLException {
        String sql = "INSERT INTO appareil (reparation_id, marque, modele, imei_serie, description, etat_physique, "
                + "probleme_signale, statut) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, a.getReparationId());
            ps.setString(2, a.getMarque());
            ps.setString(3, a.getModele());
            ps.setString(4, a.getImeiSerie());
            ps.setString(5, a.getDescription());
            ps.setString(6, a.getEtatPhysique());
            ps.setString(7, a.getProblemeSignale());
            ps.setString(8, a.getStatut().name());
            ps.executeUpdate();
            return DaoUtil.idGenere(ps);
        }
    }

    public List<Appareil> listerParReparation(Connection conn, int reparationId) throws SQLException {
        List<Appareil> liste = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(SELECT + "WHERE reparation_id = ? ORDER BY id")) {
            ps.setInt(1, reparationId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    liste.add(mapper(rs));
                }
            }
        }
        return liste;
    }

    public Optional<Appareil> trouverParId(Connection conn, int id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(SELECT + "WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapper(rs)) : Optional.empty();
            }
        }
    }

    public void modifierStatut(Connection conn, int id, StatutReparation statut) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE appareil SET statut = ?, date_maj_statut = CURRENT_TIMESTAMP WHERE id = ?")) {
            ps.setString(1, statut.name());
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    /**
     * Montant des appareils déjà livrés + de l'appareil qu'on s'apprête à livrer (RG-18).
     */
    public BigDecimal montantLivreOuALivrer(Connection conn, int reparationId, int appareilALivrer)
            throws SQLException {
        String sql = "SELECT COALESCE(SUM(i.prix_pieces_client + i.main_oeuvre), 0) "
                + "FROM intervention i JOIN appareil a ON a.id = i.appareil_id "
                + "WHERE a.reparation_id = ? AND (a.statut = 'DELIVERED' OR a.id = ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, reparationId);
            ps.setInt(2, appareilALivrer);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getBigDecimal(1);
            }
        }
    }

    private Appareil mapper(ResultSet rs) throws SQLException {
        Appareil a = new Appareil();
        a.setId(rs.getInt("id"));
        a.setReparationId(rs.getInt("reparation_id"));
        a.setMarque(rs.getString("marque"));
        a.setModele(rs.getString("modele"));
        a.setImeiSerie(rs.getString("imei_serie"));
        a.setDescription(rs.getString("description"));
        a.setEtatPhysique(rs.getString("etat_physique"));
        a.setProblemeSignale(rs.getString("probleme_signale"));
        a.setStatut(StatutReparation.valueOf(rs.getString("statut")));
        a.setDateMajStatut(DaoUtil.dateHeure(rs, "date_maj_statut"));
        return a;
    }
}
