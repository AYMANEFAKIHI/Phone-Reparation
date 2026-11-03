package ma.phonerepair.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import ma.phonerepair.model.Credit;
import ma.phonerepair.model.RemboursementCredit;

/** Accès aux tables credit_interboutique et remboursement_credit (RG-40 à RG-44). */
public interface CreditDAO {

    /** @return l'id généré */
    int inserer(Connection conn, Credit c) throws SQLException;

    void insererRemboursement(Connection conn, RemboursementCredit r) throws SQLException;

    /** SELECT ... FOR UPDATE pendant un remboursement. */
    void verrouiller(Connection conn, int creditId) throws SQLException;

    /** Un crédit avec son historique de remboursements. */
    Optional<Credit> trouverParId(Connection conn, int id) throws SQLException;

    /** Tous les crédits (plus récents d'abord) avec leurs remboursements. */
    List<Credit> listerTous(Connection conn) throws SQLException;
}
