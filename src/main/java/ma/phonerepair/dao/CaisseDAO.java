package ma.phonerepair.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import ma.phonerepair.model.MouvementCaisse;
import ma.phonerepair.model.ResumeCaisse;
import ma.phonerepair.model.enums.TypeMouvement;

/**
 * Accès aux tables caisse et mouvement_caisse.
 * Le solde n'est jamais stocké : le service le calcule à partir des totaux par type.
 */
public class CaisseDAO {

    private static final String SELECT_CAISSE =
            "SELECT c.id AS caisse_id, c.reparateur_id, u.nom_complet, u.actif FROM caisse c "
            + "JOIN reparateur r ON r.id = c.reparateur_id JOIN utilisateur u ON u.id = r.utilisateur_id ";

    /** @return l'id de la caisse créée */
    public int creer(Connection conn, int reparateurId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO caisse (reparateur_id) VALUES (?)", Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, reparateurId);
            ps.executeUpdate();
            return DaoUtil.idGenere(ps);
        }
    }

    /** Caisse d'un réparateur (identité seulement, montants complétés par le service). */
    public Optional<ResumeCaisse> trouverParReparateur(Connection conn, int reparateurId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(SELECT_CAISSE + "WHERE c.reparateur_id = ?")) {
            ps.setInt(1, reparateurId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapper(rs)) : Optional.empty();
            }
        }
    }

    public List<ResumeCaisse> listerToutes(Connection conn) throws SQLException {
        List<ResumeCaisse> liste = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(SELECT_CAISSE + "ORDER BY u.nom_complet");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                liste.add(mapper(rs));
            }
        }
        return liste;
    }

    /**
     * Verrouille la caisse jusqu'à la fin de la transaction (SELECT ... FOR UPDATE) :
     * deux sorties simultanées ne peuvent pas rendre le solde négatif.
     */
    public void verrouiller(Connection conn, int caisseId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT id FROM caisse WHERE id = ? FOR UPDATE")) {
            ps.setInt(1, caisseId);
            ps.executeQuery().close();
        }
    }

    /** @return l'id généré */
    public int insererMouvement(Connection conn, MouvementCaisse m) throws SQLException {
        String sql = "INSERT INTO mouvement_caisse (caisse_id, type, montant, paiement_id, utilisateur_id, libelle) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, m.getCaisseId());
            ps.setString(2, m.getType().name());
            ps.setBigDecimal(3, m.getMontant());
            if (m.getPaiementId() == null) {
                ps.setNull(4, Types.INTEGER);
            } else {
                ps.setInt(4, m.getPaiementId());
            }
            ps.setInt(5, m.getUtilisateurId());
            ps.setString(6, m.getLibelle());
            ps.executeUpdate();
            return DaoUtil.idGenere(ps);
        }
    }

    /** Somme des montants par type de mouvement (types absents = 0). */
    public Map<TypeMouvement, BigDecimal> totauxParType(Connection conn, int caisseId) throws SQLException {
        Map<TypeMouvement, BigDecimal> totaux = new EnumMap<>(TypeMouvement.class);
        for (TypeMouvement t : TypeMouvement.values()) {
            totaux.put(t, BigDecimal.ZERO.setScale(2));
        }
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT type, SUM(montant) FROM mouvement_caisse WHERE caisse_id = ? GROUP BY type")) {
            ps.setInt(1, caisseId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    totaux.put(TypeMouvement.valueOf(rs.getString(1)), rs.getBigDecimal(2));
                }
            }
        }
        return totaux;
    }

    /** Relevé de la caisse, le plus récent d'abord. */
    public List<MouvementCaisse> listerMouvements(Connection conn, int caisseId) throws SQLException {
        String sql = "SELECT m.id, m.caisse_id, m.type, m.montant, m.date_mouvement, m.paiement_id, m.utilisateur_id, "
                + "m.libelle, u.nom_complet FROM mouvement_caisse m JOIN utilisateur u ON u.id = m.utilisateur_id "
                + "WHERE m.caisse_id = ? ORDER BY m.date_mouvement DESC, m.id DESC LIMIT 1000";
        List<MouvementCaisse> liste = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, caisseId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    MouvementCaisse m = new MouvementCaisse();
                    m.setId(rs.getInt("id"));
                    m.setCaisseId(rs.getInt("caisse_id"));
                    m.setType(TypeMouvement.valueOf(rs.getString("type")));
                    m.setMontant(rs.getBigDecimal("montant"));
                    m.setDateMouvement(DaoUtil.dateHeure(rs, "date_mouvement"));
                    int paiementId = rs.getInt("paiement_id");
                    m.setPaiementId(rs.wasNull() ? null : paiementId);
                    m.setUtilisateurId(rs.getInt("utilisateur_id"));
                    m.setLibelle(rs.getString("libelle"));
                    m.setNomUtilisateur(rs.getString("nom_complet"));
                    liste.add(m);
                }
            }
        }
        return liste;
    }

    private ResumeCaisse mapper(ResultSet rs) throws SQLException {
        ResumeCaisse r = new ResumeCaisse();
        r.setCaisseId(rs.getInt("caisse_id"));
        r.setReparateurId(rs.getInt("reparateur_id"));
        r.setNomReparateur(rs.getString("nom_complet"));
        r.setActif(rs.getBoolean("actif"));
        return r;
    }
}
