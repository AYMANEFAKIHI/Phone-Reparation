# Plan d'équipe — RepairTrack

> Version 0.1 — 14/10/2026. Planning sur **6 semaines** (à ajuster selon la date de soutenance).
> La répartition par package est un guide de collaboration : chacun peut toucher un autre package après en avoir compris les interfaces et prévenu son propriétaire.

## 1. Rôles

| Membre | Rôle | Responsabilités principales | Packages / classes |
|--------|------|-----------------------------|--------------------|
| **M1** (chef de projet) | Lead / architecture / intégration | Docs, Git, revues de code, `app`, `AuthService`, `BoutiqueService`, `ReparateurService`, utilitaires, intégration finale | `app`, `util`, `service` (auth, boutique, réparateur) |
| **M2** | Gestion des réparations | Clients, réparations, appareils, interventions (saisie), statuts, historique, code de suivi, suivi client (service) | `Client`, `Reparation`, `Appareil`, `Intervention`, `HistoriqueStatut` + DAO/services associés, `SuiviService` |
| **M3** | Base de données / DAO | `schema.sql`, `data.sql`, tous les DAO « socle » (boutique, utilisateur, réparateur, client), `DataAccessException`, tests DAO, optimisation / index, support des autres membres sur le SQL | `database/`, `dao` |
| **M4** | Finances | Paiements, commissions, caisses, crédits inter-boutiques, tous les calculs financiers + leurs tests | `Paiement`, `Caisse`, `MouvementCaisse`, `Credit`, `RemboursementCredit` + DAO/services, `MoneyUtil` |
| **M5** | Swing / statistiques / QA | Login, Dashboard, thème, tous les panels, `StatistiqueService` + `StatistiqueDAO`, plan de tests fonctionnels, scénarios de démo | `presentation`, statistiques |

**Binômes de relecture** (chaque PR relue par au moins une personne ≠ auteur) : M2 ↔ M4, M3 ↔ M5, M1 relit tout ce qui touche `service` et la BD.

## 2. Organisation Git

- Branches : `main` (versions démontrables), `develop` (intégration), `feature/<sujet>`, `fix/<sujet>`.
- **Jamais de commit direct sur `main` ni `develop`** : tout passe par une Pull Request vers `develop`, relue et approuvée.
- Fusion `develop` → `main` par M1 à la fin de chaque jalon, avec un tag (`v0.1`, `v0.2`…).
- Messages de commit : `type(portée): description` — ex. `feat(paiement): calcul du reste à payer`, `fix(dao): fermeture du ResultSet`, `docs: règles de commission`, `test(commission): taux figé`.
- Avant d'ouvrir une PR : `git pull --rebase origin develop`, *Project > Clean* sans erreur dans Eclipse, tous les tests JUnit verts (*Run As > JUnit Test* sur `src/test/java`).
- `db.properties` n'est **jamais** commité (dans `.gitignore`).

| Branche | Membre | Jalon |
|---------|--------|-------|
| `feature/project-setup` | M1 | J1 |
| `feature/database` | M3 | J1 |
| `feature/authentication` | M1 | J1 |
| `feature/ui-shell` | M5 | J1 |
| `feature/repair-management` | M2 | J2 |
| `feature/payments` | M4 | J3 |
| `feature/cash-register` | M4 | J3 |
| `feature/credits` | M4 | J4 |
| `feature/statistics` | M5 | J4 |
| `feature/customer-tracking` | M2 + M5 | J2 |

## 3. Jalons

| Jalon | Semaine | Contenu | Démo de fin de jalon |
|-------|---------|---------|----------------------|
| **J0** | S0 (fait) | Analyse, cahier des charges, exigences, règles, architecture, plan | Validation des docs + réponses Q1–Q6 |
| **J1 — Socle** ⏳ revue | S1 | Projet Eclipse, BD, connexion, enums, modèles, utilitaires, login, dashboard selon rôle, boutique, réparateurs | S1 + S2 |
| **J2 — Réparations** ⏳ revue | S2 | Clients, réparation multi-appareils, interventions, statuts + historique, suivi client | S3, S4, S7, S9 |
| **J3 — Finances** ⏳ revue | S3 | Paiements, commissions, caisses | S5, S6, S8 |
| **J4 — Crédits & stats** ⏳ revue | S4 | Crédits inter-boutiques, statistiques, performance réparateurs | S10 + statistiques |
| **J5 — Qualité** | S5 | Tests, validation, gestion d'erreurs, polish UI, UML final | Démo complète 20 étapes |
| **J6 — Soutenance** | S6 | Docs finales, guide utilisateur, présentation, répétition | Présentation |

## 4. Tâches

Difficulté : ★ facile, ★★ moyenne, ★★★ difficile. Statut : `TODO` / `EN COURS` / `REVUE` / `FAIT`.

### J1 — Socle

| ID | Tâche | Membre | Package | Dépendance | Priorité | Diff. | Statut | Critère d'acceptation |
|----|-------|--------|---------|------------|----------|-------|--------|-----------------------|
| T01 | Créer le repo GitHub, `main`/`develop`, protections de branches, `.gitignore`, README | M1 | — | — | MUST | ★ | TODO | Les 5 membres ont cloné, push direct sur `main` refusé |
| T02 | Projet Eclipse (`.project`, `.classpath`, `.settings`, Java 17, UTF-8, JUnit 5, `lib/mysql-connector-j.jar`), arborescence des 6 packages | M1 | app | T01 | MUST | ★ | REVUE | Import Eclipse sans erreur sur les 5 postes |
| T03 | `schema.sql` (13 tables, PK, FK, UNIQUE, CHECK, index) | M3 | database | T01 | MUST | ★★★ | REVUE | Script rejouable sur base vide sans erreur ; contraintes testées à la main |
| T04 | `data.sql` (boutique, propriétaire, 2 réparateurs, clients, réparations de démo) | M3 | database | T03 | MUST | ★★ | REVUE | Login `owner` possible après import |
| T05 | `docs/database.md` (MCD, MLD, contraintes, index, choix) | M3 | docs | T03 | MUST | ★★ | REVUE | Relu par M1 |
| T06 | `DatabaseConfig`, `DatabaseConnection`, `db.properties.example` | M1 | util | T02 | MUST | ★ | REVUE | Message clair si BD absente / mauvais mot de passe |
| T07 | Enums (`Role`, `StatutReparation` + transitions, `StatutGlobal`, `StatutPaiement`, `TypePaiement`, `TypeMouvement`, `SensCredit`, `StatutCredit`) | M2 + M4 | model/enums | T02 | MUST | ★★ | REVUE | `StatutReparation.peutPasserA` couvert par tests |
| T08 | Classes modèle (15 classes) | M2, M4 | model | T07 | MUST | ★ | REVUE | Aucun import `java.sql`, montants en `BigDecimal` |
| T09 | `PasswordUtil` (PBKDF2), `MoneyUtil`, `ValidationUtil`, `DateUtil` + tests | M1 | util | T02 | MUST | ★★ | REVUE | Tests JUnit verts |
| T10 | `DataAccessException`, `BoutiqueDAO`, `UtilisateurDAO`, `ReparateurDAO` | M3 | dao | T03, T08 | MUST | ★★ | REVUE | `PreparedStatement` + try-with-resources partout |
| T11 | Exceptions service + helper `Autorisation` | M1 | service | T08 | MUST | ★ | REVUE | Utilisé par AuthService |
| T12 | `AuthService` (login, compte désactivé) | M1 | service | T09, T10 | MUST | ★★ | REVUE | Tests : bon / mauvais mot de passe / désactivé |
| T13 | `BoutiqueService`, `ReparateurService` (création transactionnelle utilisateur + réparateur + caisse, changement de taux, désactivation, profil réparateur du propriétaire) | M1 | service | T10, T12 | MUST | ★★ | REVUE | S1, S2 OK |
| T14 | `UiTheme`, `UiUtil`, `LoginFrame`, `DashboardFrame` (menu selon rôle, CardLayout), `Main`/`Application` | M5 | presentation, app | T12 | MUST | ★★ | REVUE | Owner et réparateur voient des menus différents |
| T15 | `BoutiquePanel`, `ReparateursPanel` | M5 | presentation/proprietaire | T13, T14 | MUST | ★★ | REVUE | S1, S2 faisables depuis l'UI |

### J2 — Réparations

| ID | Tâche | Membre | Package | Dépendance | Priorité | Diff. | Statut | Critère d'acceptation |
|----|-------|--------|---------|------------|----------|-------|--------|-----------------------|
| T16 | `ClientDAO`, `ClientService` (recherche, anti-doublon) | M2 (+M3 relit) | dao, service | T10 | MUST | ★ | REVUE | EF-07 |
| T17 | `CodeGenerator` + `ReparationDAO.prochainNumero(annee)` + test | M2 | util, dao | T03 | MUST | ★★ | REVUE | Format `REP-2026-000001`, collision gérée |
| T18 | `ReparationDAO`, `AppareilDAO`, `HistoriqueStatutDAO` | M2 | dao | T03, T08 | MUST | ★★ | REVUE | Jointures correctes |
| T19 | `ReparationService.creer` (transaction réparation + appareils + historique) | M2 | service | T16–T18 | MUST | ★★★ | REVUE | S3 ; rollback testé |
| T20 | `InterventionDAO` + `AppareilService.enregistrerIntervention` (copie du taux) | M2 + M4 | dao, service | T18, T25 | MUST | ★★ | REVUE | S4 ; taux figé |
| T21 | `AppareilService.changerStatut` (transitions, historique, confirmation impayé) + `calculerStatutGlobal` | M2 | service | T18 | MUST | ★★ | REVUE | S9 ; tests statut global |
| T22 | `SuiviService` (vue client filtrée) | M2 | service | T21, T26 | MUST | ★★ | REVUE | Aucun coût interne exposé |
| T23 | `NouvelleReparationPanel`, `MesReparationsPanel`, `DetailReparationDialog`, `InterventionDialog` | M5 | presentation/reparateur | T19–T21 | MUST | ★★★ | REVUE | S3, S4 depuis l'UI |
| T24 | `SuiviReparationPanel` (accessible depuis le login) | M5 | presentation/client | T22 | MUST | ★★ | REVUE | S7 |

### J3 — Finances

| ID | Tâche | Membre | Package | Dépendance | Priorité | Diff. | Statut | Critère d'acceptation |
|----|-------|--------|---------|------------|----------|-------|--------|-----------------------|
| T25 | `CommissionService` (calcul, arrondi, commission acquise/due) + tests | M4 | service | T08, T09 | MUST | ★★ | REVUE | S6 ; test 50 % → 60 % |
| T26 | `PaiementDAO`, `PaiementService` (validation, situation financière) + tests | M4 | dao, service | T18 | MUST | ★★★ | REVUE | S5, S8 ; montant > reste refusé |
| T27 | `CaisseDAO`, `CaisseService` (mouvement auto en transaction, solde, mouvements manuels) | M4 | dao, service | T26 | MUST | ★★★ | REVUE | Caisse +200 après S5 ; solde jamais négatif |
| T28 | `PaiementPanel`, `MaCaissePanel`, `CaissesPanel` | M5 | presentation | T26, T27 | MUST | ★★ | REVUE | S5, S8 depuis l'UI |

### J4 — Crédits & statistiques

| ID | Tâche | Membre | Package | Dépendance | Priorité | Diff. | Statut | Critère d'acceptation |
|----|-------|--------|---------|------------|----------|-------|--------|-----------------------|
| T29 | `CreditDAO`, `CreditService` + tests | M4 | dao, service | T03 | MUST | ★★ | REVUE | S10 |
| T30 | `CreditsPanel` | M5 | presentation/proprietaire | T29 | MUST | ★★ | REVUE | S10 depuis l'UI |
| T31 | `StatistiqueDAO` (requêtes agrégées par période) | M3 | dao | T26, T27 | MUST | ★★★ | REVUE | Résultats = calcul manuel sur `data.sql` |
| T32 | `StatistiqueService` + `StatistiquesPanel` (jour/semaine/mois/total) | M5 | service, presentation | T31 | MUST | ★★ | REVUE | EF-21 valeurs attendues |
| T33 | Performance par réparateur | M5 | service, presentation | T32 | SHOULD | ★★ | REVUE | EF-22 |

### J5 / J6 — Qualité et soutenance

| ID | Tâche | Membre | Package | Dépendance | Priorité | Diff. | Statut | Critère d'acceptation |
|----|-------|--------|---------|------------|----------|-------|--------|-----------------------|
| T34 | Diagrammes UML (cas d'utilisation, classes, 8 séquences, activité) conformes au code | M1 + tous | docs/uml | J4 | MUST | ★★ | EN COURS | Chaque classe du diagramme existe dans le code |
| T35 | Tests DAO d'intégration (base `repairtrack_test`) | M3 | test | J4 | SHOULD | ★★ | TODO | JUnit vert dans Eclipse |
| T36 | Plan de tests fonctionnels + exécution des 10 scénarios | M5 | docs/testing.md | J4 | MUST | ★ | TODO | 10/10 OK |
| T37 | Revue globale : SQL dans l'UI, `double`, exceptions avalées | M1 | tous | J4 | MUST | ★★ | TODO | 0 problème CRITICAL |
| T38 | README, guide d'installation, guide utilisateur | M1 + M5 | docs | J5 | MUST | ★ | TODO | Un camarade installe le projet seul en < 15 min |
| T39 | Ticket de dépôt imprimable | M5 | presentation | J3 | COULD | ★★ | TODO |
| T41 | Export CSV des statistiques (EF-25) | M5 | presentation | T32 | COULD | ★ | REVUE | — |
| T40 | Présentation + scénario de démo + répétition | Tous | — | J5 | MUST | ★★ | TODO | Démo 20 étapes < 15 min |

## 5. Rituels

- **Point d'équipe** 2×/semaine (15 min) : fait / à faire / bloqué.
- **Revue de jalon** en fin de semaine : démo sur `develop`, fusion vers `main`.
- Le tableau des tâches (ce fichier ou un GitHub Project) est mis à jour par chaque membre.
