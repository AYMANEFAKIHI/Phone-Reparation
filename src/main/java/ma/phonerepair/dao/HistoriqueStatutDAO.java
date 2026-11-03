package ma.phonerepair.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import ma.phonerepair.model.HistoriqueStatut;

/** Accès à la table historique_statut (RG-16). */
public interface HistoriqueStatutDAO {

    void inserer(Connection conn, HistoriqueStatut h) throws SQLException;

    List<HistoriqueStatut> listerParAppareil(Connection conn, int appareilId) throws SQLException;
}
