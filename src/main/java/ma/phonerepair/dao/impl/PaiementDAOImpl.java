package ma.phonerepair.dao.impl;

import static ma.phonerepair.dao.JdbcUtil.*;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import ma.phonerepair.dao.PaiementDAO;
import ma.phonerepair.model.Paiement;
import ma.phonerepair.model.enums.TypePaiement;

public class PaiementDAOImpl implements PaiementDAO {

    @Override
    public BigDecimal totalPaye(Connection conn, int reparationId) throws SQLException {
        return montant(conn, "SELECT " + SqlCommun.paye("?"), reparationId);
    }

    @Override
    public int inserer(Connection conn, Paiement p) throws SQLException {
        return ajouter(conn, "INSERT INTO paiement (reparation_id, montant, type_paiement, utilisateur_id, note) "
                + "VALUES (?, ?, ?, ?, ?)",
                p.getReparationId(), p.getMontant(), p.getTypePaiement(), p.getUtilisateurId(), p.getNote());
    }

    @Override
    public List<Paiement> listerParReparation(Connection conn, int reparationId) throws SQLException {
        return liste(conn, "SELECT p.*, u.nom_complet FROM paiement p JOIN utilisateur u ON u.id = p.utilisateur_id "
                + "WHERE p.reparation_id = ? ORDER BY p.date_paiement, p.id", this::mapper, reparationId);
    }

    private Paiement mapper(ResultSet rs) throws SQLException {
        Paiement p = new Paiement();
        p.setId(rs.getInt("id"));
        p.setReparationId(rs.getInt("reparation_id"));
        p.setMontant(rs.getBigDecimal("montant"));
        p.setDatePaiement(dateHeure(rs, "date_paiement"));
        p.setTypePaiement(enumeration(rs, "type_paiement", TypePaiement.class));
        p.setUtilisateurId(rs.getInt("utilisateur_id"));
        p.setNomUtilisateur(rs.getString("nom_complet"));
        p.setNote(rs.getString("note"));
        return p;
    }
}
