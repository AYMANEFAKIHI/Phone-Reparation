package ma.phonerepair.dao.impl;

import static ma.phonerepair.dao.JdbcUtil.*;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import ma.phonerepair.dao.StatistiqueDAO;
import ma.phonerepair.model.Intervention;

/**
 * Les calculs (marges, commissions) sont faits en Java par le service, pas en SQL,
 * pour réutiliser les mêmes formules que le reste de l'application.
 */
public class StatistiqueDAOImpl implements StatistiqueDAO {

    @Override
    public int compterReparations(Connection conn, LocalDateTime debut, LocalDateTime fin) throws SQLException {
        return nombre(conn, "SELECT COUNT(*) FROM reparation WHERE 1 = 1 " + bornes("date_creation", debut, fin),
                params(debut, fin));
    }

    @Override
    public int compterAppareilsLivres(Connection conn, LocalDateTime debut, LocalDateTime fin) throws SQLException {
        return nombre(conn, "SELECT COUNT(*) FROM appareil WHERE statut = 'DELIVERED' "
                + bornes("date_maj_statut", debut, fin), params(debut, fin));
    }

    @Override
    public List<Intervention> interventionsLivrees(Connection conn, LocalDateTime debut, LocalDateTime fin)
            throws SQLException {
        return liste(conn, "SELECT i.* FROM intervention i JOIN appareil a ON a.id = i.appareil_id "
                + "WHERE a.statut = 'DELIVERED' " + bornes("a.date_maj_statut", debut, fin),
                InterventionDAOImpl::mapper, params(debut, fin));
    }

    @Override
    public Map<Integer, Integer> appareilsLivresParReparateur(Connection conn, LocalDateTime debut, LocalDateTime fin)
            throws SQLException {
        return map(conn, "SELECT r.reparateur_id, COUNT(*) FROM appareil a JOIN reparation r ON r.id = a.reparation_id "
                + "WHERE a.statut = 'DELIVERED' " + bornes("a.date_maj_statut", debut, fin) + "GROUP BY r.reparateur_id",
                rs -> rs.getInt(1), rs -> rs.getInt(2), params(debut, fin));
    }

    @Override
    public BigDecimal totalEncaisse(Connection conn, LocalDateTime debut, LocalDateTime fin) throws SQLException {
        return montant(conn, "SELECT COALESCE(SUM(montant), 0) FROM paiement WHERE 1 = 1 "
                + bornes("date_paiement", debut, fin), params(debut, fin));
    }

    /** Σ des restes positifs. */
    @Override
    public BigDecimal resteAEncaisserGlobal(Connection conn) throws SQLException {
        return montant(conn, "SELECT COALESCE(SUM(GREATEST(" + SqlCommun.total("r.id") + " - "
                + SqlCommun.paye("r.id") + ", 0)), 0) FROM reparation r");
    }

    @Override
    public Map<Integer, String> nomsReparateurs(Connection conn) throws SQLException {
        return map(conn, "SELECT r.id, u.nom_complet FROM reparateur r JOIN utilisateur u ON u.id = r.utilisateur_id "
                + "ORDER BY u.nom_complet", rs -> rs.getInt(1), rs -> rs.getString(2));
    }

    /** Fragment SQL des bornes (noms de colonnes en dur — jamais de saisie utilisateur). */
    private static String bornes(String colonne, LocalDateTime debut, LocalDateTime fin) {
        return (debut == null ? "" : "AND " + colonne + " >= ? ") + (fin == null ? "" : "AND " + colonne + " < ? ");
    }

    /** Valeurs des « ? » de bornes(), dans le même ordre. */
    private static Object[] params(LocalDateTime debut, LocalDateTime fin) {
        return Stream.of(debut, fin).filter(d -> d != null).toArray();
    }
}
