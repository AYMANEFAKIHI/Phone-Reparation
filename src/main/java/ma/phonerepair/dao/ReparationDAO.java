package ma.phonerepair.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import ma.phonerepair.model.Reparation;
import ma.phonerepair.model.ResumeReparation;

/** Accès à la table reparation (commande + code de suivi). */
public interface ReparationDAO {

    /** Prochain numéro de code de suivi pour l'année. */
    int prochainNumero(Connection conn, int annee) throws SQLException;

    /** @return l'id généré */
    int inserer(Connection conn, Reparation r) throws SQLException;

    /** Réparation + client + nom du responsable (sans les appareils). */
    Optional<Reparation> trouverParId(Connection conn, int id) throws SQLException;

    Optional<Reparation> trouverParCode(Connection conn, String code) throws SQLException;

    /** SELECT ... FOR UPDATE : évite deux paiements simultanés au-delà du reste. */
    void verrouiller(Connection conn, int reparationId) throws SQLException;

    /** Total des appareils non annulés (RG-24). */
    BigDecimal montantTotal(Connection conn, int reparationId) throws SQLException;

    /**
     * Liste résumée, la plus récente d'abord.
     *
     * @param reparateurId null = toutes les réparations (propriétaire)
     * @param texte        null ou vide = pas de filtre ; sinon code, nom ou téléphone du client
     */
    List<ResumeReparation> lister(Connection conn, Integer reparateurId, String texte) throws SQLException;
}
