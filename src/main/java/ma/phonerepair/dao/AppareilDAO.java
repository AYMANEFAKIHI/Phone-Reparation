package ma.phonerepair.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import ma.phonerepair.model.Appareil;
import ma.phonerepair.model.enums.StatutReparation;

/** Accès à la table appareil. */
public interface AppareilDAO {

    /** @return l'id généré */
    int inserer(Connection conn, Appareil a) throws SQLException;

    List<Appareil> listerParReparation(Connection conn, int reparationId) throws SQLException;

    Optional<Appareil> trouverParId(Connection conn, int id) throws SQLException;

    void modifierStatut(Connection conn, int id, StatutReparation statut) throws SQLException;

    /** Montant des appareils déjà livrés + de celui qu'on s'apprête à livrer (RG-18). */
    BigDecimal montantLivreOuALivrer(Connection conn, int reparationId, int appareilALivrer) throws SQLException;
}
