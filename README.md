# RepairTrack

Application desktop de gestion et de suivi des réparations de smartphones pour une boutique.
Projet universitaire — équipe de 5 étudiants.

**Stack** : Java 17 · Swing · JDBC (MySQL Connector/J **8.0.28**) · MySQL 8 · **Lombok** · projet **Eclipse** (sans Maven) — sans framework.

## État du projet

**Toutes les fonctionnalités MUST sont implémentées** (jalons J1 à J4, en revue) : connexion et rôles, boutique, réparateurs et commissions, clients, réparations multi-appareils avec un code de suivi, interventions avec taux figé, statuts et historique, suivi client, paiements partiels, caisses, crédits inter-boutiques, statistiques jour / semaine / mois / total. Les 10 scénarios de démo se font depuis l'interface. Prochain : **J5 — Qualité** (tests JUnit sur base, guide utilisateur, revue finale). Voir `TODO.md`.

## Documentation

| Document | Contenu |
|----------|---------|
| [Cahier des charges](docs/cahier-des-charges.md) | Contexte, objectifs, périmètre |
| [Exigences](docs/requirements.md) | Matrice des fonctionnalités, scénarios de démo |
| [Règles de gestion](docs/business-rules.md) | Statuts, paiements, commissions, caisses, crédits |
| [Architecture](docs/architecture.md) | Couches, packages, transactions, erreurs |
| [Plan d'équipe](docs/team-plan.md) | Rôles, Git, jalons, tâches |
| [Base de données](docs/database.md) | MCD, MLD, contraintes, index |
| [Plan de développement](docs/development-plan.md) | Audit, roadmap, risques |
| [Tests](docs/testing.md) | Tests unitaires et scénarios |
| [UML](docs/uml/) | Cas d'utilisation, classes, séquences, activité |

## Arborescence

```
RepairTrack/
├── .project, .classpath, .settings/   configuration Eclipse (partagée dans Git)
├── RepairTrack.launch                 lancement de l'application
├── Tests RepairTrack.launch           lancement de tous les tests JUnit 5
├── lib/mysql-connector-java-8.0.28.jar driver JDBC MySQL
├── lib/lombok.jar                     Lombok (getters / setters des modèles)
├── database/schema.sql, data.sql      création de la base
├── db.properties.example              modèle de configuration (copier en db.properties)
├── docs/                              cahier des charges, règles, UML, tests…
└── src/
    ├── main/java/ma/phonerepair/      app, dao (interfaces) + dao/impl, model, presentation, service, util
    └── test/java/ma/phonerepair/      tests JUnit 5
```

## Installation

### Prérequis
- JDK 17 ou plus récent
- Eclipse IDE for Java Developers (2022-03 ou plus récent)
- MySQL 8.0.16+ (+ MySQL Workbench conseillé)
- Dans `lib/` : `mysql-connector-java-8.0.28.jar` et `lombok.jar` (voir `lib/LISEZ-MOI.txt`)
- **Lombok installé dans Eclipse** (une fois par poste : `java -jar lib/lombok.jar` > *Install / Update*)

### 1. Créer la base

```bash
mysql -u root -p < database/schema.sql   # ATTENTION : efface la base repairtrack existante
mysql -u root -p < database/data.sql     # crée le compte propriétaire
```

Sous Windows avec MySQL Workbench : *File > Open SQL Script…* puis l'éclair ⚡ (exécute tout le script), **d'abord `schema.sql`, ensuite `data.sql`**.

Créer ensuite un utilisateur dédié à l'application (évite d'utiliser root) :

```sql
CREATE USER IF NOT EXISTS 'repairtrack'@'localhost' IDENTIFIED BY 'repair2026';
CREATE USER IF NOT EXISTS 'repairtrack'@'127.0.0.1' IDENTIFIED BY 'repair2026';
GRANT ALL PRIVILEGES ON repairtrack.* TO 'repairtrack'@'localhost';
GRANT ALL PRIVILEGES ON repairtrack.* TO 'repairtrack'@'127.0.0.1';
FLUSH PRIVILEGES;
```

### 2. Configurer la connexion

Copier `db.properties.example` en **`db.properties`** à la racine du projet (3 lignes : `db.url`, `db.user`, `db.password`).
Adapter le **port** si besoin (`SELECT @@port;` dans Workbench — 3306 par défaut, parfois 3307).
Ce fichier est ignoré par Git : ne jamais le commiter.

| Message au démarrage | Cause |
|----------------------|-------|
| `Communications link failure` | mauvais port / adresse, ou MySQL arrêté (`services.msc` > MySQL > Démarrer) |
| `Access denied for user …` | mauvais utilisateur ou mot de passe |
| `Unknown database 'repairtrack'` | `schema.sql` pas exécuté (ou exécuté après `data.sql`) |
| `Fichier db.properties introuvable` | fichier absent de la racine, ou nommé `db.properties.txt` |

### 3. Importer dans Eclipse

1. Déposer `mysql-connector-java-8.0.28.jar` et `lombok.jar` dans `lib/`, puis installer Lombok dans Eclipse (voir `lib/LISEZ-MOI.txt`).
2. *File > Import… > General > Existing Projects into Workspace* → sélectionner le dossier du projet → *Finish*.
   Le projet s'appelle **RepairTrack** ; Java 17, l'encodage UTF-8 et JUnit 5 sont déjà configurés.
3. Vérifier qu'aucune erreur rouge n'apparaît dans *Problems*. Erreurs du type « The method getNom() is undefined » = Lombok pas installé dans Eclipse ; « missing required library » = un `.jar` manque dans `lib/`.

> Ne pas utiliser « Existing Maven Projects » : le projet n'utilise pas Maven.

### 4. Lancer

| Quoi | Comment |
|------|---------|
| Application | *Run > Run Configurations… > Java Application > **RepairTrack*** (ou clic droit sur `app/Main.java` > *Run As > Java Application*) |
| Tests unitaires | clic droit sur `src/test/java` > *Run As > JUnit Test* (ou la configuration **Tests RepairTrack**) |

Le répertoire de travail est la racine du projet : c'est là que doivent se trouver `db.properties` et que sera créé `logs/`.

### 5. Première connexion

| Utilisateur | Mot de passe |
|-------------|--------------|
| `owner` | `owner123` — à changer via le bouton « Mot de passe » |

Ensuite : **Boutique** → saisir les informations, **Réparateurs** → créer les comptes (et « Je répare aussi » si le propriétaire répare).

Les erreurs techniques sont écrites dans `logs/repairtrack.log`.
