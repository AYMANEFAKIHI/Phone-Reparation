package ma.phonerepair.dao.impl;

import static ma.phonerepair.dao.JdbcUtil.*;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import ma.phonerepair.dao.CaisseDAO;
import ma.phonerepair.model.MouvementCaisse;
import ma.phonerepair.model.ResumeCaisse;
import ma.phonerepair.model.enums.TypeMouvement;

public class CaisseDAOImpl implements CaisseDAO {

    private static final String SELECT_CAISSE =
            "SELECT c.id AS caisse_id, c.reparateur_id, u.nom_complet, u.actif FROM caisse c "
            + "JOIN reparateur r ON r.id = c.reparateur_id JOIN utilisateur u ON u.id = r.utilisateur_id ";

    @Override
    public int creer(Connection conn, int reparateurId) throws SQLException {
        return ajouter(conn, "INSERT INTO caisse (reparateur_id) VALUES (?)", reparateurId);
    }

    @Override
    public Optional<ResumeCaisse> trouverParReparateur(Connection conn, int reparateurId) throws SQLException {
        return un(conn, SELECT_CAISSE + "WHERE c.reparateur_id = ?", this::mapperCaisse, reparateurId);
    }

    @Override
    public List<ResumeCaisse> listerToutes(Connection conn) throws SQLException {
        return liste(conn, SELECT_CAISSE + "ORDER BY u.nom_complet", this::mapperCaisse);
    }

    @Override
    public void verrouiller(Connection conn, int caisseId) throws SQLException {
        verrouillerLigne(conn, "SELECT id FROM caisse WHERE id = ? FOR UPDATE", caisseId);
    }

    @Override
    public int insererMouvement(Connection conn, MouvementCaisse m) throws SQLException {
        return ajouter(conn, "INSERT INTO mouvement_caisse (caisse_id, type, montant, paiement_id, utilisateur_id, "
                + "libelle) VALUES (?, ?, ?, ?, ?, ?)",
                m.getCaisseId(), m.getType(), m.getMontant(), m.getPaiementId(), m.getUtilisateurId(), m.getLibelle());
    }

    @Override
    public Map<TypeMouvement, BigDecimal> totauxParType(Connection conn, int caisseId) throws SQLException {
        Map<TypeMouvement, BigDecimal> totaux = new EnumMap<>(TypeMouvement.class);
        Arrays.stream(TypeMouvement.values()).forEach(t -> totaux.put(t, BigDecimal.ZERO.setScale(2)));
        totaux.putAll(map(conn, "SELECT type, SUM(montant) FROM mouvement_caisse WHERE caisse_id = ? GROUP BY type",
                rs -> TypeMouvement.valueOf(rs.getString(1)), rs -> rs.getBigDecimal(2), caisseId));
        return totaux;
    }

    @Override
    public List<MouvementCaisse> listerMouvements(Connection conn, int caisseId) throws SQLException {
        return liste(conn, "SELECT m.*, u.nom_complet FROM mouvement_caisse m "
                + "JOIN utilisateur u ON u.id = m.utilisateur_id "
                + "WHERE m.caisse_id = ? ORDER BY m.date_mouvement DESC, m.id DESC LIMIT 1000",
                this::mapperMouvement, caisseId);
    }

    private ResumeCaisse mapperCaisse(ResultSet rs) throws SQLException {
        ResumeCaisse r = new ResumeCaisse();
        r.setCaisseId(rs.getInt("caisse_id"));
        r.setReparateurId(rs.getInt("reparateur_id"));
        r.setNomReparateur(rs.getString("nom_complet"));
        r.setActif(rs.getBoolean("actif"));
        return r;
    }

    private MouvementCaisse mapperMouvement(ResultSet rs) throws SQLException {
        MouvementCaisse m = new MouvementCaisse();
        m.setId(rs.getInt("id"));
        m.setCaisseId(rs.getInt("caisse_id"));
        m.setType(enumeration(rs, "type", TypeMouvement.class));
        m.setMontant(rs.getBigDecimal("montant"));
        m.setDateMouvement(dateHeure(rs, "date_mouvement"));
        m.setPaiementId(entier(rs, "paiement_id"));
        m.setUtilisateurId(rs.getInt("utilisateur_id"));
        m.setLibelle(rs.getString("libelle"));
        m.setNomUtilisateur(rs.getString("nom_complet"));
        return m;
    }
}
