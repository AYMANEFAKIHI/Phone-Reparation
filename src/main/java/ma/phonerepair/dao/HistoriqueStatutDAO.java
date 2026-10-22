package ma.phonerepair.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import ma.phonerepair.model.HistoriqueStatut;
import ma.phonerepair.model.enums.StatutReparation;

/** Accès à la table historique_statut (RG-16). */
public class HistoriqueStatutDAO {

    public void inserer(Connection conn, HistoriqueStatut h) throws SQLException {
        String sql = "INSERT INTO historique_statut (appareil_id, ancien_statut, nouveau_statut, utilisateur_id, "
                + "commentaire) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, h.getAppareilId());
            ps.setString(2, h.getAncienStatut() == null ? null : h.getAncienStatut().name());
            ps.setString(3, h.getNouveauStatut().name());
            ps.setInt(4, h.getUtilisateurId());
            ps.setString(5, h.getCommentaire());
            ps.executeUpdate();
        }
    }

    public List<HistoriqueStatut> listerParAppareil(Connection conn, int appareilId) throws SQLException {
        String sql = "SELECT h.id, h.appareil_id, h.ancien_statut, h.nouveau_statut, h.date_changement, "
                + "h.utilisateur_id, u.nom_complet, h.commentaire FROM historique_statut h "
                + "JOIN utilisateur u ON u.id = h.utilisateur_id WHERE h.appareil_id = ? ORDER BY h.id";
        List<HistoriqueStatut> liste = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, appareilId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    HistoriqueStatut h = new HistoriqueStatut();
                    h.setId(rs.getInt("id"));
                    h.setAppareilId(rs.getInt("appareil_id"));
                    String ancien = rs.getString("ancien_statut");
                    h.setAncienStatut(ancien == null ? null : StatutReparation.valueOf(ancien));
                    h.setNouveauStatut(StatutReparation.valueOf(rs.getString("nouveau_statut")));
                    h.setDateChangement(DaoUtil.dateHeure(rs, "date_changement"));
                    h.setUtilisateurId(rs.getInt("utilisateur_id"));
                    h.setNomUtilisateur(rs.getString("nom_complet"));
                    h.setCommentaire(rs.getString("commentaire"));
                    liste.add(h);
                }
            }
        }
        return liste;
    }
}
