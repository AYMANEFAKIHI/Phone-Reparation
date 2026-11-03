package ma.phonerepair.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import ma.phonerepair.model.Utilisateur;

/** Accès à la table utilisateur (chaque lecture charge aussi le profil réparateur éventuel). */
public interface UtilisateurDAO {

    Optional<Utilisateur> trouverParUsername(Connection conn, String username) throws SQLException;

    Optional<Utilisateur> trouverParId(Connection conn, int id) throws SQLException;

    /** Propriétaire en premier, puis par nom. */
    List<Utilisateur> listerTous(Connection conn) throws SQLException;

    /** @return l'id généré */
    int inserer(Connection conn, Utilisateur u) throws SQLException;

    void modifierActif(Connection conn, int id, boolean actif) throws SQLException;

    void modifierMotDePasse(Connection conn, int id, String passwordHash) throws SQLException;

    void modifierInfos(Connection conn, int id, String nomComplet, String telephone) throws SQLException;
}
