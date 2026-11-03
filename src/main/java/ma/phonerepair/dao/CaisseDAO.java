package ma.phonerepair.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import ma.phonerepair.model.MouvementCaisse;
import ma.phonerepair.model.ResumeCaisse;
import ma.phonerepair.model.enums.TypeMouvement;

/** Accès aux tables caisse et mouvement_caisse. Le solde n'est jamais stocké. */
public interface CaisseDAO {

    /** @return l'id de la caisse créée */
    int creer(Connection conn, int reparateurId) throws SQLException;

    Optional<ResumeCaisse> trouverParReparateur(Connection conn, int reparateurId) throws SQLException;

    List<ResumeCaisse> listerToutes(Connection conn) throws SQLException;

    /** SELECT ... FOR UPDATE : deux sorties simultanées ne peuvent pas rendre le solde négatif. */
    void verrouiller(Connection conn, int caisseId) throws SQLException;

    /** @return l'id généré */
    int insererMouvement(Connection conn, MouvementCaisse m) throws SQLException;

    /** Somme des montants par type de mouvement (types absents = 0). */
    Map<TypeMouvement, BigDecimal> totauxParType(Connection conn, int caisseId) throws SQLException;

    /** Relevé de la caisse, le plus récent d'abord. */
    List<MouvementCaisse> listerMouvements(Connection conn, int caisseId) throws SQLException;
}
