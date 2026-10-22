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
import ma.phonerepair.model.Reparation;
import ma.phonerepair.model.ResumeReparation;
import ma.phonerepair.model.enums.StatutReparation;
import ma.phonerepair.util.CodeGenerator;

/** Accès à la table reparation (commande + code de suivi). */
public class ReparationDAO {

    /**
     * Montant total d'une réparation (RG-24) : prix pièces client + main d'œuvre des
     * interventions des appareils NON annulés. Fragment unique réutilisé partout.
     * Paramètre attendu : l'expression SQL donnant l'id de la réparation.
     */
    static String sousRequeteTotal(String idReparation) {
        return "(SELECT COALESCE(SUM(i.prix_pieces_client + i.main_oeuvre), 0) "
                + "FROM intervention i JOIN appareil a ON a.id = i.appareil_id "
                + "WHERE a.reparation_id = " + idReparation + " AND a.statut <> 'CANCELLED')";
    }

    static String sousRequetePaye(String idReparation) {
        return "(SELECT COALESCE(SUM(p.montant), 0) FROM paiement p WHERE p.reparation_id = " + idReparation + ")";
    }

    private static final String SELECT_DETAIL =
            "SELECT r.id, r.code_suivi, r.client_id, r.reparateur_id, r.cree_par, r.date_creation, r.remarque, "
            + "c.nom_complet AS client_nom, c.telephone AS client_tel, u.nom_complet AS reparateur_nom "
            + "FROM reparation r "
            + "JOIN client c ON c.id = r.client_id "
            + "JOIN reparateur rp ON rp.id = r.reparateur_id "
            + "JOIN utilisateur u ON u.id = rp.utilisateur_id ";

    /**
     * Prochain numéro de l'année. Les codes sont complétés par des zéros :
     * l'ordre alphabétique est l'ordre numérique, MAX() suffit.
     */
    public int prochainNumero(Connection conn, int annee) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT MAX(code_suivi) FROM reparation WHERE code_suivi LIKE ?")) {
            ps.setString(1, CodeGenerator.prefixeAnnee(annee) + "%");
            try (ResultSet rs = ps.executeQuery()) {
                String max = rs.next() ? rs.getString(1) : null;
                return max == null ? 1 : CodeGenerator.numero(max) + 1;
            }
        }
    }

    /** @return l'id généré */
    public int inserer(Connection conn, Reparation r) throws SQLException {
        String sql = "INSERT INTO reparation (code_suivi, client_id, reparateur_id, cree_par, remarque) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, r.getCodeSuivi());
            ps.setInt(2, r.getClient().getId());
            ps.setInt(3, r.getReparateurId());
            ps.setInt(4, r.getCreePar());
            ps.setString(5, r.getRemarque());
            ps.executeUpdate();
            return DaoUtil.idGenere(ps);
        }
    }

    /** Réparation + client + nom du responsable (sans les appareils). */
    public Optional<Reparation> trouverParId(Connection conn, int id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(SELECT_DETAIL + "WHERE r.id = ?")) {
            ps.setInt(1, id);
            return lireUne(ps);
        }
    }

    public Optional<Reparation> trouverParCode(Connection conn, String code) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(SELECT_DETAIL + "WHERE r.code_suivi = ?")) {
            ps.setString(1, code);
            return lireUne(ps);
        }
    }

    /** Verrouille la réparation jusqu'à la fin de la transaction (évite deux paiements simultanés au-delà du reste). */
    public void verrouiller(Connection conn, int reparationId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT id FROM reparation WHERE id = ? FOR UPDATE")) {
            ps.setInt(1, reparationId);
            ps.executeQuery().close();
        }
    }

    public BigDecimal montantTotal(Connection conn, int reparationId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT " + sousRequeteTotal("?"))) {
            ps.setInt(1, reparationId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getBigDecimal(1);
            }
        }
    }

    /**
     * Liste résumée, la plus récente d'abord.
     *
     * @param reparateurId null = toutes les réparations (propriétaire)
     * @param texte        null ou vide = pas de filtre ; sinon code, nom ou téléphone du client
     */
    public List<ResumeReparation> lister(Connection conn, Integer reparateurId, String texte) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT r.id, r.code_suivi, r.date_creation, r.reparateur_id, "
                + "c.id AS client_id, c.nom_complet AS client_nom, c.telephone AS client_tel, "
                + "u.nom_complet AS reparateur_nom, "
                + "(SELECT GROUP_CONCAT(a.statut ORDER BY a.id) FROM appareil a WHERE a.reparation_id = r.id) AS statuts, "
                + sousRequeteTotal("r.id") + " AS total, "
                + sousRequetePaye("r.id") + " AS paye "
                + "FROM reparation r "
                + "JOIN client c ON c.id = r.client_id "
                + "JOIN reparateur rp ON rp.id = r.reparateur_id "
                + "JOIN utilisateur u ON u.id = rp.utilisateur_id WHERE 1 = 1 ");
        List<Object> params = new ArrayList<>();
        if (reparateurId != null) {
            sql.append("AND r.reparateur_id = ? ");
            params.add(reparateurId);
        }
        if (texte != null && !texte.isBlank()) {
            sql.append("AND (r.code_suivi LIKE ? OR c.nom_complet LIKE ? OR c.telephone LIKE ?) ");
            String motif = "%" + texte.trim() + "%";
            params.add(motif);
            params.add(motif);
            params.add(motif);
        }
        sql.append("ORDER BY r.date_creation DESC, r.id DESC LIMIT 500");

        List<ResumeReparation> liste = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    liste.add(mapperResume(rs));
                }
            }
        }
        return liste;
    }

    private Optional<Reparation> lireUne(PreparedStatement ps) throws SQLException {
        try (ResultSet rs = ps.executeQuery()) {
            if (!rs.next()) {
                return Optional.empty();
            }
            Reparation r = new Reparation();
            r.setId(rs.getInt("id"));
            r.setCodeSuivi(rs.getString("code_suivi"));
            r.setClient(ClientDAO.mapper(rs, "client_id", "client_nom", "client_tel"));
            r.setReparateurId(rs.getInt("reparateur_id"));
            r.setNomReparateur(rs.getString("reparateur_nom"));
            r.setCreePar(rs.getInt("cree_par"));
            r.setDateCreation(DaoUtil.dateHeure(rs, "date_creation"));
            r.setRemarque(rs.getString("remarque"));
            return Optional.of(r);
        }
    }

    private ResumeReparation mapperResume(ResultSet rs) throws SQLException {
        ResumeReparation r = new ResumeReparation();
        r.setId(rs.getInt("id"));
        r.setCodeSuivi(rs.getString("code_suivi"));
        r.setDateCreation(DaoUtil.dateHeure(rs, "date_creation"));
        r.setReparateurId(rs.getInt("reparateur_id"));
        r.setNomReparateur(rs.getString("reparateur_nom"));
        r.setClient(ClientDAO.mapper(rs, "client_id", "client_nom", "client_tel"));
        String statuts = rs.getString("statuts");
        List<StatutReparation> liste = new ArrayList<>();
        if (statuts != null) {
            for (String s : statuts.split(",")) {
                liste.add(StatutReparation.valueOf(s));
            }
        }
        r.setStatutsAppareils(liste);
        r.setTotal(rs.getBigDecimal("total"));
        r.setPaye(rs.getBigDecimal("paye"));
        return r;
    }
}
