package ma.phonerepair.dao.impl;

import static ma.phonerepair.dao.JdbcUtil.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import ma.phonerepair.dao.BoutiqueDAO;
import ma.phonerepair.model.Boutique;

public class BoutiqueDAOImpl implements BoutiqueDAO {

    @Override
    public Optional<Boutique> trouver(Connection conn) throws SQLException {
        return un(conn, "SELECT * FROM boutique WHERE id = 1", this::mapper);
    }

    @Override
    public void inserer(Connection conn, Boutique b) throws SQLException {
        executer(conn, "INSERT INTO boutique (id, nom, adresse, telephone, numero_patente) VALUES (1, ?, ?, ?, ?)",
                b.getNom(), b.getAdresse(), b.getTelephone(), b.getNumeroPatente());
    }

    @Override
    public void modifier(Connection conn, Boutique b) throws SQLException {
        executer(conn, "UPDATE boutique SET nom = ?, adresse = ?, telephone = ?, numero_patente = ? WHERE id = 1",
                b.getNom(), b.getAdresse(), b.getTelephone(), b.getNumeroPatente());
    }

    private Boutique mapper(ResultSet rs) throws SQLException {
        Boutique b = new Boutique();
        b.setId(rs.getInt("id"));
        b.setNom(rs.getString("nom"));
        b.setAdresse(rs.getString("adresse"));
        b.setTelephone(rs.getString("telephone"));
        b.setNumeroPatente(rs.getString("numero_patente"));
        b.setDateCreation(dateHeure(rs, "date_creation"));
        return b;
    }
}
