package ma.phonerepair.dao.impl;

import static ma.phonerepair.dao.JdbcUtil.*;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import ma.phonerepair.dao.ReparationDAO;
import ma.phonerepair.model.Reparation;
import ma.phonerepair.model.ResumeReparation;
import ma.phonerepair.model.enums.StatutReparation;
import ma.phonerepair.util.CodeGenerator;

public class ReparationDAOImpl implements ReparationDAO {

    private static final String JOINTURES = "FROM reparation r "
            + "JOIN client c ON c.id = r.client_id "
            + "JOIN reparateur rp ON rp.id = r.reparateur_id "
            + "JOIN utilisateur u ON u.id = rp.utilisateur_id ";

    private static final String SELECT_DETAIL = "SELECT r.*, c.nom_complet AS client_nom, "
            + "c.telephone AS client_tel, u.nom_complet AS reparateur_nom " + JOINTURES;

    private static final String SELECT_RESUME = "SELECT r.id, r.code_suivi, r.date_creation, r.reparateur_id, "
            + "r.client_id, c.nom_complet AS client_nom, c.telephone AS client_tel, u.nom_complet AS reparateur_nom, "
            + "(SELECT GROUP_CONCAT(a.statut ORDER BY a.id) FROM appareil a WHERE a.reparation_id = r.id) AS statuts, "
            + SqlCommun.total("r.id") + " AS total, " + SqlCommun.paye("r.id") + " AS paye " + JOINTURES;

    /** Codes complétés par des zéros : l'ordre alphabétique est l'ordre numérique, MAX() suffit. */
    @Override
    public int prochainNumero(Connection conn, int annee) throws SQLException {
        return un(conn, "SELECT MAX(code_suivi) FROM reparation WHERE code_suivi LIKE ?", rs -> rs.getString(1),
                CodeGenerator.prefixeAnnee(annee) + "%")
                .map(max -> CodeGenerator.numero(max) + 1)
                .orElse(1);
    }

    @Override
    public int inserer(Connection conn, Reparation r) throws SQLException {
        return ajouter(conn, "INSERT INTO reparation (code_suivi, client_id, reparateur_id, cree_par, remarque) "
                + "VALUES (?, ?, ?, ?, ?)",
                r.getCodeSuivi(), r.getClient().getId(), r.getReparateurId(), r.getCreePar(), r.getRemarque());
    }

    @Override
    public Optional<Reparation> trouverParId(Connection conn, int id) throws SQLException {
        return un(conn, SELECT_DETAIL + "WHERE r.id = ?", this::mapperDetail, id);
    }

    @Override
    public Optional<Reparation> trouverParCode(Connection conn, String code) throws SQLException {
        return un(conn, SELECT_DETAIL + "WHERE r.code_suivi = ?", this::mapperDetail, code);
    }

    @Override
    public void verrouiller(Connection conn, int reparationId) throws SQLException {
        verrouillerLigne(conn, "SELECT id FROM reparation WHERE id = ? FOR UPDATE", reparationId);
    }

    @Override
    public BigDecimal montantTotal(Connection conn, int reparationId) throws SQLException {
        return montant(conn, "SELECT " + SqlCommun.total("?"), reparationId);
    }

    @Override
    public List<ResumeReparation> lister(Connection conn, Integer reparateurId, String texte) throws SQLException {
        StringBuilder sql = new StringBuilder(SELECT_RESUME).append("WHERE 1 = 1 ");
        List<Object> params = new ArrayList<>();
        if (reparateurId != null) {
            sql.append("AND r.reparateur_id = ? ");
            params.add(reparateurId);
        }
        if (texte != null && !texte.isBlank()) {
            sql.append("AND (r.code_suivi LIKE ? OR c.nom_complet LIKE ? OR c.telephone LIKE ?) ");
            String motif = "%" + texte.trim() + "%";
            params.addAll(List.of(motif, motif, motif));
        }
        sql.append("ORDER BY r.date_creation DESC, r.id DESC LIMIT 500");
        return liste(conn, sql.toString(), this::mapperResume, params.toArray());
    }

    private Reparation mapperDetail(ResultSet rs) throws SQLException {
        Reparation r = new Reparation();
        r.setId(rs.getInt("id"));
        r.setCodeSuivi(rs.getString("code_suivi"));
        r.setClient(ClientDAOImpl.mapper(rs, "client_id", "client_nom", "client_tel"));
        r.setReparateurId(rs.getInt("reparateur_id"));
        r.setNomReparateur(rs.getString("reparateur_nom"));
        r.setCreePar(rs.getInt("cree_par"));
        r.setDateCreation(dateHeure(rs, "date_creation"));
        r.setRemarque(rs.getString("remarque"));
        return r;
    }

    private ResumeReparation mapperResume(ResultSet rs) throws SQLException {
        ResumeReparation r = new ResumeReparation();
        r.setId(rs.getInt("id"));
        r.setCodeSuivi(rs.getString("code_suivi"));
        r.setDateCreation(dateHeure(rs, "date_creation"));
        r.setReparateurId(rs.getInt("reparateur_id"));
        r.setNomReparateur(rs.getString("reparateur_nom"));
        r.setClient(ClientDAOImpl.mapper(rs, "client_id", "client_nom", "client_tel"));
        String statuts = rs.getString("statuts");
        r.setStatutsAppareils(statuts == null ? new ArrayList<>()
                : new ArrayList<>(Arrays.stream(statuts.split(",")).map(StatutReparation::valueOf).toList()));
        r.setTotal(rs.getBigDecimal("total"));
        r.setPaye(rs.getBigDecimal("paye"));
        return r;
    }
}
