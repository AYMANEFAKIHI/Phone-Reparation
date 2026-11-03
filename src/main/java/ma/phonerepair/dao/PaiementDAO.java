package ma.phonerepair.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import ma.phonerepair.model.Paiement;

/** Accès à la table paiement (un versement client = une ligne, RG-25). */
public interface PaiementDAO {

    BigDecimal totalPaye(Connection conn, int reparationId) throws SQLException;

    /** @return l'id généré */
    int inserer(Connection conn, Paiement p) throws SQLException;

    /** Du plus ancien au plus récent. */
    List<Paiement> listerParReparation(Connection conn, int reparationId) throws SQLException;
}
