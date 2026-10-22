# Plan de développement — RepairTrack

## 1. Audit initial (Phase 0 — 14/10/2026)

| Point | Constat |
|-------|---------|
| Structure existante | Un seul fichier : `java.txt` (notes du sujet en français). Aucun code, aucun `pom.xml`, aucun script SQL, pas de dépôt Git. |
| Technologies | Aucune encore installée dans le projet. Cible : Java 17, Eclipse (sans Maven), Swing, JDBC, MySQL 8. |
| Code / BD existants | Aucun → projet **from scratch**, pas de code à préserver. |
| Infos apportées par `java.txt` | « **sans gestion de stock** » (confirmé hors périmètre) ; « le prop prend juste la commission » (**contradiction possible** avec le prompt où le taux = part du réparateur → Q1) ; « gestion de crédit entre prop et prop avec num de magasin » ; « nombre de caisses = nombre de réparateurs ». |
| Incohérences relevées dans la spécification | `DatabaseConnection` placé dans `app` alors que les DAO en dépendent ; dossier `database/` listé dans le package Java ; modèles manquants pour 3 tables exigées (historique, mouvement, remboursement) ; codes de suivi séquentiels devinables (risque de fuite d'infos clients). Toutes traitées dans `architecture.md` § 3 et `business-rules.md` § 12. |

## 2. Roadmap

1. **Phase 0–2 — Analyse** ✅ : audit, cahier des charges, exigences, règles, architecture, plan d'équipe.
2. **Validation** ⏳ : réponses aux questions Q1–Q6, relecture par l'équipe.
3. **Phase 3–4 — Conception** : UML (cas d'utilisation, classes), `database.md`, `schema.sql`, `data.sql`.
4. **Jalon J1 — Socle** : projet Eclipse, connexion, modèles, login, dashboard, boutique, réparateurs.
5. **J2 — Réparations** → **J3 — Finances** → **J4 — Crédits & stats** → **J5 — Qualité** → **J6 — Soutenance**.

## 3. Risques

| Risque | Impact | Probabilité | Mitigation |
|--------|--------|-------------|------------|
| Règles financières mal comprises (commission, caisse) | Élevé | Moyenne | Questions Q1–Q3 tranchées **avant** `schema.sql` ; tests unitaires sur chaque formule |
| Conflits Git entre 5 membres | Moyen | Élevée | Petites PR, `pull --rebase` avant PR, propriétaires de packages, M1 intègre |
| UI bloquée par le retard du backend | Moyen | Moyenne | Interfaces de service définies en J1 ; M5 commence par Login/Dashboard/thème |
| Montants faux à cause de `double` | Élevé | Faible | `BigDecimal` imposé, vérifié en revue (T37) |
| Configuration MySQL différente sur chaque PC | Moyen | Élevée | `db.properties.example`, guide d'installation, même version MySQL 8 |
| Code non compris par un membre lors de la soutenance | Élevé | Moyenne | Revue croisée obligatoire, chaque membre présente un module qui n'est pas le sien en répétition |
| Périmètre trop large | Moyen | Moyenne | MoSCoW strict ; COULD seulement après J4 |
| Statistiques lentes / requêtes complexes | Faible | Faible | Index sur dates, requêtes agrégées côté SQL |
