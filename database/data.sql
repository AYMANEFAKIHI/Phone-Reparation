-- =====================================================================
-- RepairTrack — données initiales
-- À exécuter APRÈS schema.sql : mysql -u root -p < database/data.sql
--
-- Crée uniquement le compte propriétaire. La boutique et les réparateurs
-- sont créés depuis l'application (scénarios de démo S1 et S2).
--
--   username : owner
--   password : owner123   ← à changer après la première connexion
--
-- Pour générer un autre hash :
--   Eclipse : clic droit sur util/PasswordUtil.java > Run As > Run Configurations…
--   > onglet Arguments > Program arguments = <motDePasse> > Run
-- =====================================================================

SET NAMES utf8mb4;
USE repairtrack;

INSERT INTO utilisateur (nom_complet, telephone, username, password_hash, role, actif)
VALUES ('Propriétaire', NULL, 'owner',
        'pbkdf2$120000$tYwTiyzrJRZU/LlJVs7a6g==$NUoBC68jlox+ycbbfjSVVlEhCZQKsHCdoAf/KQXnW7k=',
        'PROPRIETAIRE', TRUE);
