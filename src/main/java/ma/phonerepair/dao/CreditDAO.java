package ma.phonerepair.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import ma.phonerepair.model.Credit;
import ma.phonerepair.model.RemboursementCredit;
import ma.phonerepair.model.enums.SensCredit;

/** Accès aux tables credit_interboutique et remboursement_credit (RG-40 à RG-44). */
public class CreditDAO {

    private static final String SELECT = "SELECT id, sens, nom_boutique_tiers, proprietaire_tiers, patente_tiers, "
            + "telephone_tiers, montant, date_credit, description, cree_par FROM credit_interboutique ";

    /** @return l'id généré */
    public int inserer(Connection conn, Credit c) throws SQLException {
        String sql = "INSERT INTO credit_interboutique (sens, nom_boutique_tiers, proprietaire_tiers, patente_tiers, "
                + "telephone_tiers, montant, date_credit, description, cree_par) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getSens().name());
            ps.setString(2, c.getNomBoutiqueTiers());
            ps.setString(3, c.getProprietaireTiers());
            ps.setString(4, c.getPatenteTiers());
            ps.setString(5, c.getTelephoneTiers());
            ps.setBigDecimal(6, c.getMontant());
            ps.setDate(7, Date.valueOf(c.getDateCredit()));
            ps.setString(8, c.getDescription());
            ps.setInt(9, c.getCreePar());
            ps.executeUpdate();
            return DaoUtil.idGenere(ps);
        }
    }

    public void insererRemboursement(Connection conn, RemboursementCredit r) throws SQLException {
        String sql = "INSERT INTO remboursement_credit (credit_id, montant, date_remboursement, note, utilisateur_id) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, r.getCreditId());
            ps.setBigDecimal(2, r.getMontant());
            ps.setDate(3, Date.valueOf(r.getDateRemboursement()));
            ps.setString(4, r.getNote());
            ps.setInt(5, r.getUtilisateurId());
            ps.executeUpdate();
        }
    }

    /** Verrouille le crédit pendant un remboursement (évite deux remboursements au-delà du reste). */
    public void verrouiller(Connection conn, int creditId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT id FROM credit_interboutique WHERE id = ? FOR UPDATE")) {
            ps.setInt(1, creditId);
            ps.executeQuery().close();
        }
    }

    /** Un crédit avec son historique de remboursements. */
    public Optional<Credit> trouverParId(Connection conn, int id) throws SQLException {
        List<Credit> l;
        try (PreparedStatement ps = conn.prepareStatement(SELECT + "WHERE id = ?")) {
            ps.setInt(1, id);
            l = lireCredits(ps);
        }
        if (l.isEmpty()) {
            return Optional.empty();
        }
        Credit c = l.get(0);
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM remboursement_credit WHERE credit_id = ? ORDER BY date_remboursement, id")) {
            ps.setInt(1, id);
            c.setRemboursements(lireRemboursements(ps));
        }
        return Optional.of(c);
    }

    /** Tous les crédits (plus récents d'abord) avec leurs remboursements, en 2 requêtes. */
    public List<Credit> listerTous(Connection conn) throws SQLException {
        List<Credit> credits;
        try (PreparedStatement ps = conn.prepareStatement(SELECT + "ORDER BY date_credit DESC, id DESC")) {
            credits = lireCredits(ps);
        }
        Map<Integer, List<RemboursementCredit>> parCredit;
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM remboursement_credit ORDER BY date_remboursement, id")) {
            parCredit = lireRemboursements(ps).stream()
                    .collect(Collectors.groupingBy(RemboursementCredit::getCreditId));
        }
        for (Credit c : credits) {
            c.setRemboursements(parCredit.getOrDefault(c.getId(), new ArrayList<>()));
        }
        return credits;
    }

    private List<Credit> lireCredits(PreparedStatement ps) throws SQLException {
        List<Credit> liste = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Credit c = new Credit();
                c.setId(rs.getInt("id"));
                c.setSens(SensCredit.valueOf(rs.getString("sens")));
                c.setNomBoutiqueTiers(rs.getString("nom_boutique_tiers"));
                c.setProprietaireTiers(rs.getString("proprietaire_tiers"));
                c.setPatenteTiers(rs.getString("patente_tiers"));
                c.setTelephoneTiers(rs.getString("telephone_tiers"));
                c.setMontant(rs.getBigDecimal("montant"));
                c.setDateCredit(rs.getDate("date_credit").toLocalDate());
                c.setDescription(rs.getString("description"));
                c.setCreePar(rs.getInt("cree_par"));
                liste.add(c);
            }
        }
        return liste;
    }

    private List<RemboursementCredit> lireRemboursements(PreparedStatement ps) throws SQLException {
        List<RemboursementCredit> liste = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                RemboursementCredit r = new RemboursementCredit();
                r.setId(rs.getInt("id"));
                r.setCreditId(rs.getInt("credit_id"));
                r.setMontant(rs.getBigDecimal("montant"));
                r.setDateRemboursement(rs.getDate("date_remboursement").toLocalDate());
                r.setNote(rs.getString("note"));
                r.setUtilisateurId(rs.getInt("utilisateur_id"));
                liste.add(r);
            }
        }
        return liste;
    }
}
