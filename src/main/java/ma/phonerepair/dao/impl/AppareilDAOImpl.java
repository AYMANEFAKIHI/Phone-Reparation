package ma.phonerepair.dao.impl;

import static ma.phonerepair.dao.JdbcUtil.*;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import ma.phonerepair.dao.AppareilDAO;
import ma.phonerepair.model.Appareil;
import ma.phonerepair.model.enums.StatutReparation;

public class AppareilDAOImpl implements AppareilDAO {

    @Override
    public int inserer(Connection conn, Appareil a) throws SQLException {
        return ajouter(conn, "INSERT INTO appareil (reparation_id, marque, modele, imei_serie, description, "
                + "etat_physique, probleme_signale, statut) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                a.getReparationId(), a.getMarque(), a.getModele(), a.getImeiSerie(), a.getDescription(),
                a.getEtatPhysique(), a.getProblemeSignale(), a.getStatut());
    }

    @Override
    public List<Appareil> listerParReparation(Connection conn, int reparationId) throws SQLException {
        return liste(conn, "SELECT * FROM appareil WHERE reparation_id = ? ORDER BY id", this::mapper, reparationId);
    }

    @Override
    public Optional<Appareil> trouverParId(Connection conn, int id) throws SQLException {
        return un(conn, "SELECT * FROM appareil WHERE id = ?", this::mapper, id);
    }

    @Override
    public void modifierStatut(Connection conn, int id, StatutReparation statut) throws SQLException {
        executer(conn, "UPDATE appareil SET statut = ?, date_maj_statut = CURRENT_TIMESTAMP WHERE id = ?", statut, id);
    }

    @Override
    public BigDecimal montantLivreOuALivrer(Connection conn, int reparationId, int appareilALivrer)
            throws SQLException {
        return montant(conn, "SELECT COALESCE(SUM(i.prix_pieces_client + i.main_oeuvre), 0) "
                + "FROM intervention i JOIN appareil a ON a.id = i.appareil_id "
                + "WHERE a.reparation_id = ? AND (a.statut = 'DELIVERED' OR a.id = ?)", reparationId, appareilALivrer);
    }

    private Appareil mapper(ResultSet rs) throws SQLException {
        Appareil a = new Appareil();
        a.setId(rs.getInt("id"));
        a.setReparationId(rs.getInt("reparation_id"));
        a.setMarque(rs.getString("marque"));
        a.setModele(rs.getString("modele"));
        a.setImeiSerie(rs.getString("imei_serie"));
        a.setDescription(rs.getString("description"));
        a.setEtatPhysique(rs.getString("etat_physique"));
        a.setProblemeSignale(rs.getString("probleme_signale"));
        a.setStatut(enumeration(rs, "statut", StatutReparation.class));
        a.setDateMajStatut(dateHeure(rs, "date_maj_statut"));
        return a;
    }
}
