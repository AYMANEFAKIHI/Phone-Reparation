package ma.phonerepair.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import ma.phonerepair.model.Intervention;

/**
 * Requêtes de lecture pour les statistiques (EF-21, EF-22).
 * Les bornes sont [debut ; fin[ ; debut et fin null = pas de limite (période TOTAL).
 * Les calculs (marges, commissions) sont faits en Java par le service, pas en SQL,
 * pour réutiliser les mêmes formules que le reste de l'application.
 */
public class StatistiqueDAO {

    public int compterReparations(Connection conn, LocalDateTime debut, LocalDateTime fin) throws SQLException {
        return compter(conn, "SELECT COUNT(*) FROM reparation WHERE 1 = 1 " + bornes("date_creation", debut, fin),
                debut, fin);
    }

    /** Appareils livrés pendant la période (date de livraison = date_maj_statut quand statut = DELIVERED). */
    public int compterAppareilsLivres(Connection conn, LocalDateTime debut, LocalDateTime fin) throws SQLException {
        return compter(conn, "SELECT COUNT(*) FROM appareil WHERE statut = 'DELIVERED' "
                + bornes("date_maj_statut", debut, fin), debut, fin);
    }

    /** Interventions des appareils livrés pendant la période (base des indicateurs d'activité). */
    public List<Intervention> interventionsLivrees(Connection conn, LocalDateTime debut, LocalDateTime fin)
            throws SQLException {
        String sql = "SELECT i.id, i.appareil_id, i.reparateur_id, i.cout_achat_pieces, i.prix_pieces_client, "
                + "i.main_oeuvre, i.taux_commission_applique FROM intervention i "
                + "JOIN appareil a ON a.id = i.appareil_id WHERE a.statut = 'DELIVERED' "
                + bornes("a.date_maj_statut", debut, fin);
        List<Intervention> liste = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            parametres(ps, debut, fin);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Intervention i = new Intervention();
                    i.setId(rs.getInt("id"));
                    i.setAppareilId(rs.getInt("appareil_id"));
                    i.setReparateurId(rs.getInt("reparateur_id"));
                    i.setCoutAchatPieces(rs.getBigDecimal("cout_achat_pieces"));
                    i.setPrixPiecesClient(rs.getBigDecimal("prix_pieces_client"));
                    i.setMainOeuvre(rs.getBigDecimal("main_oeuvre"));
                    i.setTauxCommissionApplique(rs.getBigDecimal("taux_commission_applique"));
                    liste.add(i);
                }
            }
        }
        return liste;
    }

    /** Appareils livrés par réparateur responsable sur la période. */
    public Map<Integer, Integer> appareilsLivresParReparateur(Connection conn, LocalDateTime debut, LocalDateTime fin)
            throws SQLException {
        String sql = "SELECT r.reparateur_id, COUNT(*) FROM appareil a JOIN reparation r ON r.id = a.reparation_id "
                + "WHERE a.statut = 'DELIVERED' " + bornes("a.date_maj_statut", debut, fin) + "GROUP BY r.reparateur_id";
        Map<Integer, Integer> m = new LinkedHashMap<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            parametres(ps, debut, fin);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    m.put(rs.getInt(1), rs.getInt(2));
                }
            }
        }
        return m;
    }

    /** Paiements clients reçus pendant la période. */
    public BigDecimal totalEncaisse(Connection conn, LocalDateTime debut, LocalDateTime fin) throws SQLException {
        String sql = "SELECT COALESCE(SUM(montant), 0) FROM paiement WHERE 1 = 1 " + bornes("date_paiement", debut, fin);
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            parametres(ps, debut, fin);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getBigDecimal(1);
            }
        }
    }

    /** Reste à encaisser sur toutes les réparations, aujourd'hui (Σ des restes positifs). */
    public BigDecimal resteAEncaisserGlobal(Connection conn) throws SQLException {
        String sql = "SELECT COALESCE(SUM(GREATEST(" + ReparationDAO.sousRequeteTotal("r.id") + " - "
                + ReparationDAO.sousRequetePaye("r.id") + ", 0)), 0) FROM reparation r";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getBigDecimal(1);
        }
    }

    /** Noms des réparateurs (id profil → nom), pour la performance. */
    public Map<Integer, String> nomsReparateurs(Connection conn) throws SQLException {
        Map<Integer, String> m = new LinkedHashMap<>();
        try (PreparedStatement ps = conn.prepareStatement("SELECT r.id, u.nom_complet FROM reparateur r "
                + "JOIN utilisateur u ON u.id = r.utilisateur_id ORDER BY u.nom_complet");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                m.put(rs.getInt(1), rs.getString(2));
            }
        }
        return m;
    }

    private int compter(Connection conn, String sql, LocalDateTime debut, LocalDateTime fin) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            parametres(ps, debut, fin);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    /** Fragment SQL des bornes (noms de colonnes en dur — jamais de saisie utilisateur). */
    private static String bornes(String colonne, LocalDateTime debut, LocalDateTime fin) {
        return (debut == null ? "" : "AND " + colonne + " >= ? ") + (fin == null ? "" : "AND " + colonne + " < ? ");
    }

    private static void parametres(PreparedStatement ps, LocalDateTime debut, LocalDateTime fin) throws SQLException {
        int i = 1;
        if (debut != null) {
            ps.setTimestamp(i++, Timestamp.valueOf(debut));
        }
        if (fin != null) {
            ps.setTimestamp(i, Timestamp.valueOf(fin));
        }
    }
}
