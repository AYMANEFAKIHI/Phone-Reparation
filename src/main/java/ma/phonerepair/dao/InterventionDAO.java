package ma.phonerepair.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import ma.phonerepair.model.Intervention;

/** Accès à la table intervention (diagnostic, pièces, main d'œuvre, taux figé). */
public interface InterventionDAO {

    /** Le taux appliqué est écrit ici une fois pour toutes (RG-33). @return l'id généré */
    int inserer(Connection conn, Intervention i) throws SQLException;

    /** Modifie le contenu. Le taux de commission n'est volontairement PAS modifiable. */
    void modifierContenu(Connection conn, Intervention i) throws SQLException;

    List<Intervention> listerParAppareil(Connection conn, int appareilId) throws SQLException;

    /** Toutes les interventions d'une réparation en une seule requête. */
    List<Intervention> listerParReparation(Connection conn, int reparationId) throws SQLException;

    /**
     * @param livrees true = appareils DELIVERED (commission acquise, RG-35) ;
     *                false = appareils ni livrés ni annulés (commission à venir)
     */
    List<Intervention> listerPourCommission(Connection conn, int reparateurId, boolean livrees) throws SQLException;
}
