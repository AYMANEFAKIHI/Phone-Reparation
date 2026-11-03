package ma.phonerepair.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

/** Accès à la table reparateur (profil réparateur d'un utilisateur). */
public interface ReparateurDAO {

    /** @return l'id du profil créé */
    int inserer(Connection conn, int utilisateurId, BigDecimal tauxCommission) throws SQLException;

    /** Modifie le taux actuel. N'affecte pas les interventions existantes (RG-33). */
    void modifierTaux(Connection conn, int reparateurId, BigDecimal tauxCommission) throws SQLException;

    /** Taux actuel, à copier dans une nouvelle intervention. */
    Optional<BigDecimal> trouverTaux(Connection conn, int reparateurId) throws SQLException;
}
