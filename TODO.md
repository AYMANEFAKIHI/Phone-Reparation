# TODO — RepairTrack

> Mis à jour : 14/10/2026 (fin J4). Détail des tâches dans `docs/team-plan.md`.

## 🔴 Bloquant (avant toute ligne de code)

- [x] Q1–Q4 validées (commission = part réparateur, due à la livraison, caisse de celui qui encaisse, suivi code + 4 chiffres)
- [x] Q5 : pas de livraison sans paiement — Q6 : crédits dans les deux sens
- [ ] Relecture des docs + du code J1 par les 5 membres
- [ ] Attribuer les rôles M1–M5 aux noms réels
- [ ] Fixer la date de soutenance → ajuster le planning

## J1 — Socle

- [ ] T01 Repo GitHub + branches + protections
- [x] T02 Projet Eclipse + arborescence (Maven abandonné : Eclipse imposé)
- [x] T03 `schema.sql`
- [x] T04 `data.sql`
- [x] T05 `docs/database.md`
- [x] T06 Connexion BD + `db.properties.example`
- [x] T07 Enums
- [x] T08 Modèles
- [x] T09 Utilitaires + tests
- [x] T10 DAO socle
- [x] T11 Exceptions + autorisation
- [x] T12 `AuthService`
- [x] T13 `BoutiqueService`, `ReparateurService`
- [x] T14 Login + Dashboard
- [x] T15 Panels Boutique / Réparateurs

## J2 → J6

## J2 — Réparations (en revue)

- [x] T16 Clients (recherche, anti-doublon)
- [x] T17 Génération du code + prochain numéro
- [x] T18 DAO réparation / appareil / historique
- [x] T19 Création transactionnelle multi-appareils
- [x] T20 Intervention + taux figé
- [x] T21 Changement de statut, historique, statut global, livraison payée (Q5)
- [x] T22 Suivi client (code + 4 chiffres)
- [x] T23 Écrans nouvelle réparation / liste / détail / intervention
- [x] T24 Écran de suivi client (depuis le login)

## Suite
## J3 — Finances (en revue)

- [x] T25 Commissions (acquise à la livraison, due, à venir, taux figé)
- [x] T26 Paiements (validation, verrou, situation, historique)
- [x] T27 Caisses (encaissement automatique, solde, achats de pièces, versements, paiement de commission)
- [x] T28 Écrans Encaissement, Ma caisse, Caisses

## Suite

## J4 — Crédits & statistiques (en revue)

- [x] T29 Crédits inter-boutiques (création, remboursements partiels, reste dû, statut)
- [x] T30 Écran Crédits boutiques
- [x] T31 Requêtes statistiques par période
- [x] T32 Statistiques jour / semaine / mois / total + écran
- [x] T33 Performance par réparateur
- [x] T41 Export CSV des statistiques (COULD)
- [x] Accueil avec indicateurs (propriétaire : jour ; réparateur : caisse et commission)

## Suite

- [ ] J5 Qualité (T34–T37)
- [ ] J6 Docs & soutenance (T38–T40)

## Documents encore à créer

- [x] `docs/uml/` : cas d'utilisation, classes, 8 séquences, activité (19 diagrammes Mermaid validés) — à mettre à jour à la fin de J3/J4
- [x] `docs/testing.md` (initial — à compléter à chaque jalon)
- [ ] `docs/user-guide.md`

## Dette technique

- Importer le projet dans Eclipse sur un vrai poste (Windows) et lancer l'application : vérifié ici avec le compilateur Eclipse (ecj, 0 erreur) et JUnit 5 (67/67), mais pas dans l'IDE lui-même.
- [x] `lib/mysql-connector-j.jar` déposé (MySQL Connector/J 26.10.0, testé) — reste à le commiter dans Git.
- [x] `pom.xml` supprimé.
- Base testée sur MariaDB 10.11 (compatible) : refaire un passage de `schema.sql` sur MySQL 8 réel (T03).
- Pas encore de tests DAO automatisés (T35) : vérifications faites par des scénarios d'intégration (J1 : 18, J2 : 36, J3 : 33, J4 : 24 — toutes OK). Les transformer en tests JUnit sur une base `repairtrack_test`.
- Les scénarios d'intégration J2 insèrent encore 3 paiements en SQL direct (sans mouvement de caisse) : à réécrire avec `PaiementService.enregistrer` lors de la migration en JUnit (T35).

## Fonctionnalités incomplètes

- Annulation d'un paiement saisi par erreur (EF-26, COULD) : non faite — en attendant, corriger par un mouvement manuel n'est pas possible ; à décider en J5.
- Ticket de dépôt imprimable (EF-24, COULD) : non fait.
