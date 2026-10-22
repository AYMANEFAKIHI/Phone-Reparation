# Changelog

Format : [Keep a Changelog](https://keepachangelog.com/fr/1.1.0/).

## [Non publié]

### Modifié — 2026-10-14 (Eclipse sans Maven)
- Le projet devient un **projet Eclipse** : `.project`, `.classpath` (src/main/java, src/test/java, JRE 17, JUnit 5 d'Eclipse, `lib/mysql-connector-j.jar`), `.settings/` (Java 17, UTF-8), `RepairTrack.launch`, `Tests RepairTrack.launch`, `lib/LISEZ-MOI.txt`.
- `pom.xml` n'est plus utilisé (à supprimer). `.gitignore` adapté : la configuration Eclipse est partagée.
- README : installation et lancement dans Eclipse.

### Ajouté — 2026-10-14 (J4 — Crédits et statistiques)
- Crédits inter-boutiques : `CreditDAO`, `CreditService` (création, remboursements partiels avec verrou, reste dû, statut, totaux par sens), écran « Crédits boutiques ».
- Statistiques : `Periode`, `Statistiques`, `PerformanceReparateur`, `StatistiqueDAO`, `StatistiqueService` (12 indicateurs définis, performance par réparateur), écran « Statistiques » avec export CSV.
- Accueil avec indicateurs (propriétaire : activité du jour ; réparateur : caisse et commission).
- Règles RG-46 à RG-49 (définitions et dates des statistiques).
- 8 nouveaux tests unitaires (67 au total), 24 vérifications d'intégration ; UML mis à jour.

### Ajouté — 2026-10-14 (J3 — Finances)
- Paiements : `PaiementService.enregistrer` (paiement + mouvement ENCAISSEMENT dans la caisse de celui qui encaisse, en une transaction, avec verrou de la réparation), `chargerDossier` par code, historique des paiements.
- Caisses : `CaisseService` (solde calculé, relevé, achats de pièces, versements au propriétaire, paiement de commission avec contrôles solde / commission due).
- Commissions : acquise (appareils livrés), due, à venir — chaque intervention avec son taux figé.
- `model` : `DossierPaiement`, `ResumeCaisse` ; `CaisseDAO`, `PaiementDAO`, `InterventionDAO` complétés.
- Écrans : Encaissement (aussi depuis le détail d'une réparation), Ma caisse, Caisses (propriétaire) ; composants `CarteChiffre`, `MouvementsTableModel`.
- 5 nouveaux tests unitaires (59 au total), 33 vérifications d'intégration.
- UML mis à jour (classes, séquences paiement et commission).

### Ajouté — 2026-10-14 (J2 — Réparations)
- `docs/uml/` : cas d'utilisation, diagramme de classes, 8 diagrammes de séquence, diagramme d'activité (Mermaid).
- `dao` : `ClientDAO`, `ReparationDAO` (code de suivi, liste agrégée), `AppareilDAO`, `InterventionDAO`, `HistoriqueStatutDAO`, `PaiementDAO` (lecture).
- `service` : `ClientService`, `ReparationService` (création multi-appareils transactionnelle, statut global), `AppareilService` (intervention avec taux figé, statuts, livraison payée), `SuiviService`, `PaiementService` (situation), `CommissionService` (calculs).
- `model` : `SituationFinanciere`, `ResumeReparation`, `SuiviClient`.
- `presentation` : nouvelle réparation, liste des réparations, détail, saisie d'intervention, suivi client (aussi depuis l'écran de connexion).
- 23 nouveaux tests unitaires (54 au total).

### Modifié
- Tableau de bord ouvert en plein écran ; section « Réparations » dans le menu.
- RG-13 précisée : le responsable est le créateur de la réparation.

### Ajouté — 2026-10-14 (J1 — Socle)
- Décisions Q5 (pas de livraison sans paiement) et Q6 (crédits dans les deux sens) intégrées aux règles.
- `database/schema.sql` (13 tables, contraintes, index), `database/data.sql` (compte propriétaire), `docs/database.md`.
- Projet Maven (`pom.xml`, Java 17), `db.properties.example`.
- `util` : configuration et connexion BD, hachage PBKDF2, montants, validation, dates, code de suivi.
- `model` : 13 classes métier + 8 enums (dont la matrice des transitions de statut).
- `dao` : `TransactionManager`, `DataAccessException`, DAO boutique / utilisateur / réparateur / caisse.
- `service` : authentification, boutique, réparateurs (création transactionnelle avec caisse, taux, désactivation, profil réparateur du propriétaire), contrôle des droits.
- `presentation` : thème, écran de connexion, tableau de bord selon le rôle, écrans Boutique et Réparateurs.
- 31 tests unitaires JUnit 5, `docs/testing.md`.

### Ajouté — 2026-10-14
- Audit initial du dépôt (projet vierge, seul `java.txt` présent).
- `docs/cahier-des-charges.md`, `docs/requirements.md`, `docs/business-rules.md`, `docs/architecture.md`, `docs/team-plan.md`, `docs/development-plan.md`.
- `TODO.md`, `README.md`, `.gitignore`.
