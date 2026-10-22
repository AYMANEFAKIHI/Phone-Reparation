# Plan et résultats de tests — RepairTrack

> Mis à jour à chaque jalon. Dernière exécution : 14/10/2026 (J4).

## 1. Stratégie

| Niveau | Outil | Base de données | Quoi |
|--------|-------|-----------------|------|
| Unitaire | JUnit 5 (`src/test/java`) | non | calculs, validations, transitions, droits |
| Intégration | scénarios exécutés sur une base de test | oui | services + DAO + contraintes |
| Fonctionnel | manuel, depuis l'interface | oui | scénarios de démo S1–S10 |

Les tests unitaires ne touchent jamais la base : ils doivent passer sur n'importe quel poste (Eclipse : clic droit sur `src/test/java` > *Run As > JUnit Test*). Vérifié le 14/10 avec le compilateur Eclipse (ecj) : 0 erreur, 0 avertissement, 67/67 tests verts.

## 2. Tests unitaires (67 — tous OK)

| Classe | Tests | Règles couvertes |
|--------|-------|------------------|
| `PasswordUtilTest` | 6 | RG-04 : hash ≠ mot de passe, sel aléatoire, hash de `data.sql` valide |
| `MoneyUtilTest` | 5 | RG-21, RG-34 : commission 100 × 60 % = 60,00, arrondi HALF_UP, saisie « 1 250,50 » |
| `ValidationUtilTest` | 5 | RG-07 : téléphones marocains, username, mot de passe ≥ 6 |
| `CodeGeneratorTest` | 5 | RG-11 : format `REP-2026-000123`, limites, ordre des codes |
| `StatutReparationTest` | 4 | RG-15, RG-17 : transitions autorisées / interdites, états finaux |
| `ReparateurServiceTest` | 2 | RG-31 : taux 0–100, 2 décimales max |
| `AutorisationTest` | 4 | RG-05 : réparateur ≠ propriétaire, propriétaire avec/sans profil, compte désactivé |
| `StatutGlobalTest` | 7 | RG-19 : exemple du sujet (READY + WAITING_FOR_PART → IN_PROGRESS), tous livrés, annulés |
| `SituationFinanciereTest` | 5 | RG-27/28 : S5 (500/200 → 300, PARTIALLY_PAID), S8 (PAID), 3 paiements du sujet |
| `ValidationSaisieTest` | 6 | appareil incomplet, IMEI, S4 (300/400/100 → 500), montant négatif |
| `CommissionServiceTest` | 5 | S6 (60 / 40), pièces non commissionnées, taux figé 50 % vs 60 % |
| `CaisseCalculTest` | 5 | RG-37 solde (entrées − sorties), somme de commissions avec des taux différents |
| `CreditCalculTest` | 4 | S10 (1000 − 400 = 600, PARTIEL), en cours, soldé, totaux par sens |
| `StatistiqueCalculTest` | 4 | valeurs EF-21 (500 / 300 / 100 / 100 / 60 / 40 / 140), cohérence des définitions, bornes jour / semaine (lundi) / mois / total |

## 3. Intégration J1 (base de test, 18 vérifications — toutes OK)

| # | Vérification | Résultat attendu |
|---|--------------|------------------|
| 1 | Connexion `owner` mauvais mot de passe | « Nom d'utilisateur ou mot de passe incorrect » |
| 2 | Connexion utilisateur inconnu | même message (on ne révèle pas les comptes existants) |
| 3 | **S1** connexion owner + création boutique | boutique enregistrée, téléphone normalisé |
| 4 | Modification de la boutique | nom mis à jour |
| 5 | Téléphone invalide | `ValidationException` |
| 6 | **S2** création d'Ahmed (`ahmed`, 60 %) | Ahmed se connecte, profil 60 %, caisse créée |
| 7 | Username en double | « Le nom d'utilisateur « ahmed » est déjà utilisé » |
| 8 | Réparateur qui crée un compte | `AccesRefuseException` |
| 9 | Réparateur qui modifie la boutique | `AccesRefuseException` |
| 10 | Modification du taux (55,5 %) | nouveau taux en base |
| 11 | Compte désactivé | connexion refusée |
| 12 | Désactiver le propriétaire | refusé |
| 13 | Réinitialisation du mot de passe | connexion avec le nouveau |
| 14 | « Je répare aussi » | propriétaire avec profil + caisse, sans 2ᵉ compte |
| 15 | Activer le profil deux fois | refusé |
| 16 | Liste des comptes | 2 comptes |
| 17 | Changer son propre mot de passe | connexion avec le nouveau |
| 18 | Contraintes SQL (boutique unique, rôle, taux > 100, encaissement sans paiement) | refusées par MySQL |

## 3 bis. Intégration J2 (36 vérifications — toutes OK)

| Thème | Vérifications |
|-------|---------------|
| Clients | téléphone invalide refusé, recherche par téléphone et par nom, client existant proposé (anti-doublon) |
| **S3** | 1 réparation, 2 appareils, **un seul code** `REP-2026-000001`, 2 lignes d'historique ; code suivant `REP-2026-000002` |
| Transaction | erreur BD pendant la création → **rien** n'est enregistré (rollback) |
| Droits | propriétaire sans profil ne crée pas ; Karim ne voit ni ne modifie les réparations d'Ahmed ; listes filtrées par réparateur |
| **S4** + RG-33 | total 500 DH ; taux 60 % figé ; taux passé à 70 % → l'intervention existante garde 60 %, la nouvelle prend 70 % |
| Statuts | transition interdite refusée ; appareil livré non modifiable ; historique complet (5 lignes) |
| **S5 / S8** | 500 / 200 / 300 `PARTIALLY_PAID` puis 500 / 500 / 0 `PAID` (paiements insérés en SQL, écran en J3) |
| **S7** | suivi par code + 4 chiffres : iPhone `READY`, Samsung `WAITING_FOR_PART`, global `IN_PROGRESS`, montants visibles ; mauvais chiffres et code inconnu → même message |
| **Q5** | livraison refusée (« il manque 300,00 DH ») puis acceptée après paiement |
| **S9** | les 2 appareils livrés → réparation `DELIVERED` ; ajout d'appareil refusé sur réparation livrée |
| EF-09 | ajout d'un appareil à une réparation en cours, même code de suivi |
| Cas limites | annulation refusée si déjà payé ; baisse du total sous le montant payé refusée ; appareil non chiffré annulé sans impact |
| RG-02 | propriétaire avec profil réparateur crée une réparation |

## 3 ter. Intégration J3 (33 vérifications — toutes OK, sans aucun SQL direct)

| Thème | Vérifications |
|-------|---------------|
| Validation | paiement avant chiffrage, 0, négatif, > reste, 3 décimales → refusés ; propriétaire sans profil ne peut pas encaisser |
| **S5** | 200 DH → 500 / 200 / 300 `PARTIALLY_PAID` ; caisse d'Ahmed +200 |
| **S6** | commission 60 DH « à venir » tant que l'appareil n'est pas livré, puis **acquise** à la livraison |
| RG-36 | Karim encaisse les 300 DH restants d'une réparation d'Ahmed via le code → ils vont dans la caisse de **Karim** |
| **S8** | 500 / 500 / 0 `PAID` ; nouveau paiement refusé ; 1 paiement = 1 mouvement ENCAISSEMENT |
| **S7 / S9** | suivi : `IN_PROGRESS`, reste 0 ; tous livrés → `DELIVERED` |
| RG-33 | taux passé à 70 % après livraison → commission acquise reste 60 DH |
| Droits | caisse d'un autre, liste des caisses, versement et paiement de commission par un réparateur → refusés ; encaissement manuel interdit |
| Caisse | achat pièce 50 → solde 150 ; versement 100 → 50 ; commission 60 > solde refusée ; commission 50 → due 10, solde 0 ; commission > due refusée |
| RG-02 | propriétaire avec profil réparateur → sa propre caisse |

## 3 quater. Intégration J4 (24 vérifications — toutes OK)

| Thème | Vérifications |
|-------|---------------|
| **EF-21** (total) | 2 réparations, 2 appareils livrés ; facturé 500, coût pièces 300, marge pièces 100, main d'œuvre 100, commissions 60, part propriétaire 40, **marge brute 140** ; encaissé 600 (dont avance de 100 sur une réparation non livrée) ; reste à encaisser 150 ; argent en caisses 600 |
| Périodes | jour = aujourd'hui ; hier = 0 ; semaine et mois = 140 de marge brute ; mois précédent = 0 |
| **EF-22** | Ahmed : 2 appareils livrés, 500 DH, 60 DH de commission ; Karim : 0 |
| **S10** | crédit accordé de 1000 DH → `EN_COURS` ; remboursement 400 → reste 600, `PARTIEL` ; puis 600 → `SOLDE` ; remboursement supplémentaire refusé |
| Validation | crédit sans patente, date future, remboursement > reste ou antérieur au crédit → refusés |
| Droits | statistiques et crédits : propriétaire uniquement |
| RG-44 | les crédits ne modifient ni l'encaissé ni les caisses |

## 4. Interface (J1)

Vérifié : écran de connexion, tableau de bord propriétaire (menus Boutique, Réparateurs), tableau de bord réparateur (menus propriétaire absents), formulaire boutique, tableau et dialogue de création des réparateurs, accents corrects.

## 4 bis. Interface (J2)

Vérifié par captures : écran « Nouvelle réparation » (client + appareils + remarque), liste des réparations (statut coloré, montants), détail d'une réparation (appareils, statuts, total / payé / reste), suivi client avec code + 4 chiffres.

## 4 ter. Interface (J3)

Vérifié par captures : Encaissement (situation, historique, saisie), Ma caisse (5 indicateurs + relevé), Caisses (propriétaire : tableau des caisses, total en caisse, commissions dues).

## 4 quater. Interface (J4)

Vérifié par captures : Statistiques (12 indicateurs avec leur définition, performance par réparateur), Crédits boutiques (totaux par sens, statut), accueil propriétaire (activité du jour) et réparateur (caisse, commission).

## 5. À venir

- J5 : transformer les scénarios d'intégration (111 vérifications) en tests JUnit sur une base `repairtrack_test` (T35).
- J5 : dérouler la démo complète (20 étapes) sur MySQL 8 réel, sur le poste de soutenance.
