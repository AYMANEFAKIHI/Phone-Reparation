package ma.phonerepair.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import ma.phonerepair.model.Intervention;

/**
 * Lectures pour les statistiques (EF-21, EF-22).
 * Bornes [debut ; fin[ ; debut et fin null = pas de limite (période TOTAL).
 */
public interface StatistiqueDAO {

    int compterReparations(Connection conn, LocalDateTime debut, LocalDateTime fin) throws SQLException;

    /** Date de livraison = date_maj_statut quand statut = DELIVERED. */
    int compterAppareilsLivres(Connection conn, LocalDateTime debut, LocalDateTime fin) throws SQLException;

    /** Interventions des appareils livrés pendant la période. */
    List<Intervention> interventionsLivrees(Connection conn, LocalDateTime debut, LocalDateTime fin)
            throws SQLException;

    /** Appareils livrés par réparateur responsable (id profil → nombre). */
    Map<Integer, Integer> appareilsLivresParReparateur(Connection conn, LocalDateTime debut, LocalDateTime fin)
            throws SQLException;

    /** Paiements clients reçus pendant la période. */
    BigDecimal totalEncaisse(Connection conn, LocalDateTime debut, LocalDateTime fin) throws SQLException;

    /** Reste à encaisser sur toutes les réparations, aujourd'hui. */
    BigDecimal resteAEncaisserGlobal(Connection conn) throws SQLException;

    /** id profil réparateur → nom. */
    Map<Integer, String> nomsReparateurs(Connection conn) throws SQLException;
}
