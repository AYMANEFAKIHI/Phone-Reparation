package ma.phonerepair.dao.impl;

import static ma.phonerepair.dao.JdbcUtil.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import ma.phonerepair.dao.HistoriqueStatutDAO;
import ma.phonerepair.model.HistoriqueStatut;
import ma.phonerepair.model.enums.StatutReparation;

public class HistoriqueStatutDAOImpl implements HistoriqueStatutDAO {

    @Override
    public void inserer(Connection conn, HistoriqueStatut h) throws SQLException {
        executer(conn, "INSERT INTO historique_statut (appareil_id, ancien_statut, nouveau_statut, utilisateur_id, "
                + "commentaire) VALUES (?, ?, ?, ?, ?)",
                h.getAppareilId(), h.getAncienStatut(), h.getNouveauStatut(), h.getUtilisateurId(), h.getCommentaire());
    }

    @Override
    public List<HistoriqueStatut> listerParAppareil(Connection conn, int appareilId) throws SQLException {
        return liste(conn, "SELECT h.*, u.nom_complet FROM historique_statut h "
                + "JOIN utilisateur u ON u.id = h.utilisateur_id WHERE h.appareil_id = ? ORDER BY h.id",
                this::mapper, appareilId);
    }

    private HistoriqueStatut mapper(ResultSet rs) throws SQLException {
        HistoriqueStatut h = new HistoriqueStatut();
        h.setId(rs.getInt("id"));
        h.setAppareilId(rs.getInt("appareil_id"));
        h.setAncienStatut(enumeration(rs, "ancien_statut", StatutReparation.class));
        h.setNouveauStatut(enumeration(rs, "nouveau_statut", StatutReparation.class));
        h.setDateChangement(dateHeure(rs, "date_changement"));
        h.setUtilisateurId(rs.getInt("utilisateur_id"));
        h.setNomUtilisateur(rs.getString("nom_complet"));
        h.setCommentaire(rs.getString("commentaire"));
        return h;
    }
}
