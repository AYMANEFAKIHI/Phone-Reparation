package ma.phonerepair.dao.impl;

import static ma.phonerepair.dao.JdbcUtil.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import ma.phonerepair.dao.CreditDAO;
import ma.phonerepair.model.Credit;
import ma.phonerepair.model.RemboursementCredit;
import ma.phonerepair.model.enums.SensCredit;

public class CreditDAOImpl implements CreditDAO {

    @Override
    public int inserer(Connection conn, Credit c) throws SQLException {
        return ajouter(conn, "INSERT INTO credit_interboutique (sens, nom_boutique_tiers, proprietaire_tiers, "
                + "patente_tiers, telephone_tiers, montant, date_credit, description, cree_par) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                c.getSens(), c.getNomBoutiqueTiers(), c.getProprietaireTiers(), c.getPatenteTiers(),
                c.getTelephoneTiers(), c.getMontant(), c.getDateCredit(), c.getDescription(), c.getCreePar());
    }

    @Override
    public void insererRemboursement(Connection conn, RemboursementCredit r) throws SQLException {
        executer(conn, "INSERT INTO remboursement_credit (credit_id, montant, date_remboursement, note, "
                + "utilisateur_id) VALUES (?, ?, ?, ?, ?)",
                r.getCreditId(), r.getMontant(), r.getDateRemboursement(), r.getNote(), r.getUtilisateurId());
    }

    @Override
    public void verrouiller(Connection conn, int creditId) throws SQLException {
        verrouillerLigne(conn, "SELECT id FROM credit_interboutique WHERE id = ? FOR UPDATE", creditId);
    }

    @Override
    public Optional<Credit> trouverParId(Connection conn, int id) throws SQLException {
        Optional<Credit> credit = un(conn, "SELECT * FROM credit_interboutique WHERE id = ?", this::mapperCredit, id);
        if (credit.isPresent()) {
            credit.get().setRemboursements(liste(conn, "SELECT * FROM remboursement_credit WHERE credit_id = ? "
                    + "ORDER BY date_remboursement, id", this::mapperRemboursement, id));
        }
        return credit;
    }

    /** 2 requêtes au total : les crédits, puis tous les remboursements groupés par crédit. */
    @Override
    public List<Credit> listerTous(Connection conn) throws SQLException {
        List<Credit> credits = liste(conn, "SELECT * FROM credit_interboutique ORDER BY date_credit DESC, id DESC",
                this::mapperCredit);
        Map<Integer, List<RemboursementCredit>> parCredit = liste(conn,
                "SELECT * FROM remboursement_credit ORDER BY date_remboursement, id", this::mapperRemboursement)
                .stream()
                .collect(Collectors.groupingBy(RemboursementCredit::getCreditId));
        credits.forEach(c -> c.setRemboursements(parCredit.getOrDefault(c.getId(), new ArrayList<>())));
        return credits;
    }

    private Credit mapperCredit(ResultSet rs) throws SQLException {
        Credit c = new Credit();
        c.setId(rs.getInt("id"));
        c.setSens(enumeration(rs, "sens", SensCredit.class));
        c.setNomBoutiqueTiers(rs.getString("nom_boutique_tiers"));
        c.setProprietaireTiers(rs.getString("proprietaire_tiers"));
        c.setPatenteTiers(rs.getString("patente_tiers"));
        c.setTelephoneTiers(rs.getString("telephone_tiers"));
        c.setMontant(rs.getBigDecimal("montant"));
        c.setDateCredit(date(rs, "date_credit"));
        c.setDescription(rs.getString("description"));
        c.setCreePar(rs.getInt("cree_par"));
        return c;
    }

    private RemboursementCredit mapperRemboursement(ResultSet rs) throws SQLException {
        RemboursementCredit r = new RemboursementCredit();
        r.setId(rs.getInt("id"));
        r.setCreditId(rs.getInt("credit_id"));
        r.setMontant(rs.getBigDecimal("montant"));
        r.setDateRemboursement(date(rs, "date_remboursement"));
        r.setNote(rs.getString("note"));
        r.setUtilisateurId(rs.getInt("utilisateur_id"));
        return r;
    }
}
