# Matrice des exigences — RepairTrack

> Version 0.1 — 14/10/2026. Priorités MoSCoW : **MUST** (démo impossible sans), **SHOULD** (attendu), **COULD** (si le temps le permet), **WON'T** (exclu de cette version).
> `RG-xx` renvoie à `business-rules.md`. Acteurs : **P** = propriétaire, **R** = réparateur, **C** = client.

## 1. Vue d'ensemble

| ID | Fonctionnalité | Acteur | Priorité | Membre | Règles |
|----|----------------|--------|----------|--------|--------|
| EF-01 | Se connecter | P, R | MUST | M1/M5 | RG-03, RG-04 |
| EF-02 | Se déconnecter | P, R | MUST | M5 | — |
| EF-03 | Configurer la boutique | P | MUST | M1/M5 | RG-06, RG-07 |
| EF-04 | Créer un compte réparateur avec taux | P | MUST | M1/M5 | RG-02, RG-04, RG-31 |
| EF-05 | Modifier le taux / désactiver un réparateur | P | MUST | M1/M5 | RG-03, RG-33 |
| EF-06 | Activer le profil réparateur du propriétaire | P | MUST | M1 | RG-02 |
| EF-07 | Rechercher / créer un client | R, P | MUST | M2 | RG-08, RG-09 |
| EF-08 | Créer une réparation avec plusieurs appareils | R, P | MUST | M2 | RG-10 → RG-14 |
| EF-09 | Ajouter un appareil à une réparation existante | R, P | SHOULD | M2 | RG-10, RG-17 |
| EF-10 | Saisir une intervention (diagnostic, pièces, main d'œuvre) | R, P | MUST | M2/M4 | RG-20 → RG-24, RG-33 |
| EF-11 | Changer le statut d'un appareil | R, P | MUST | M2 | RG-15 → RG-19 |
| EF-12 | Lister / filtrer les réparations | R, P | MUST | M2/M5 | RG-19 |
| EF-13 | Enregistrer un paiement | R, P | MUST | M4 | RG-25 → RG-30, RG-36 |
| EF-14 | Voir la situation financière d'une réparation | R, P | MUST | M4 | RG-27, RG-28 |
| EF-15 | Calculer les commissions | Système | MUST | M4 | RG-31 → RG-35 |
| EF-16 | Voir ma caisse et ma commission | R | MUST | M4/M5 | RG-37, RG-38 |
| EF-17 | Voir toutes les caisses | P | MUST | M4/M5 | RG-37 |
| EF-18 | Mouvements manuels de caisse | P, R | SHOULD | M4 | RG-37 → RG-39 |
| EF-19 | Déclarer un crédit inter-boutique | P | MUST | M4 | RG-40 → RG-44 |
| EF-20 | Enregistrer un remboursement de crédit | P | MUST | M4 | RG-42, RG-43 |
| EF-21 | Statistiques jour / semaine / mois / total | P | MUST | M5 | § 9.2 |
| EF-22 | Performance par réparateur | P | SHOULD | M5 | § 9.2 |
| EF-23 | Suivi de réparation par code | C | MUST | M2/M5 | RG-45 |
| EF-24 | Ticket de dépôt imprimable | R | COULD | M5 | — |
| EF-25 | Export CSV des statistiques | P | COULD | M5 | — |
| EF-26 | Annulation tracée d'un paiement | P | COULD | M4 | RG-29 |
| EF-27 | Gestion de stock des pièces | — | WON'T | — | hors périmètre |
| EF-28 | Compte client / SMS / site web | — | WON'T | — | hors périmètre |

## 2. Fiches détaillées

### EF-01 — Se connecter
- **Acteur** : P, R
- **Description** : accès à l'application avec username + mot de passe.
- **Préconditions** : compte existant et actif.
- **Scénario principal** : 1. L'utilisateur saisit ses identifiants. 2. `AuthService` vérifie le hash. 3. Le tableau de bord s'ouvre avec le menu correspondant au rôle.
- **Alternatives** : identifiants faux → « Nom d'utilisateur ou mot de passe incorrect » (sans préciser lequel) ; compte désactivé → « Compte désactivé, contactez le propriétaire » ; BD inaccessible → « Impossible de se connecter à la base de données ».
- **Postconditions** : l'utilisateur connecté est transmis au `DashboardFrame`.
- **Acceptation** : `owner` voit le menu Propriétaire ; `ahmed` ne voit ni Réparateurs, ni Crédits, ni Statistiques boutique.

### EF-03 — Configurer la boutique
- **Acteur** : P
- **Préconditions** : connecté en propriétaire.
- **Scénario** : saisir nom, adresse, téléphone, N° patente → enregistrer.
- **Alternatives** : champ vide ou téléphone invalide → message sous le formulaire, rien n'est enregistré.
- **Postconditions** : la boutique est enregistrée / mise à jour.
- **Acceptation** : scénario de démo n°1.

### EF-04 — Créer un compte réparateur
- **Acteur** : P
- **Scénario** : saisir nom complet, téléphone, username, mot de passe, taux → le système crée `utilisateur` + `reparateur` + `caisse` (**transaction**).
- **Alternatives** : username déjà pris ; taux hors [0;100] ; mot de passe < 6 caractères.
- **Postconditions** : le réparateur peut se connecter ; sa caisse a un solde de 0.
- **Acceptation** : scénario n°2 (Ahmed, `ahmed`, 60 %).

### EF-05 — Modifier taux / désactiver
- **Acteur** : P
- **Scénario** : sélectionner un réparateur → nouveau taux ou désactivation.
- **Postconditions** : seules les **futures** interventions utilisent le nouveau taux (RG-33).
- **Acceptation** : une intervention créée à 50 %, taux passé à 60 % → l'ancienne affiche toujours 50 %.

### EF-06 — Profil réparateur du propriétaire
- **Acteur** : P
- **Scénario** : case « Je répare aussi » dans l'écran Boutique/Réparateurs → crée `reparateur` + `caisse` liés au même utilisateur.
- **Acceptation** : le propriétaire voit aussi les menus « Nouvelle réparation » et « Ma caisse » sans second compte.

### EF-07 — Rechercher / créer un client
- **Acteur** : R, P
- **Scénario** : saisir un téléphone ou un nom → liste des clients → sélectionner, ou « Nouveau client ».
- **Alternatives** : téléphone déjà existant à la création → proposer le client existant.
- **Acceptation** : recherche de `0612` retourne tous les clients dont le numéro commence par `0612`.

### EF-08 — Créer une réparation multi-appareils
- **Acteur** : R, P (avec profil réparateur)
- **Préconditions** : client sélectionné.
- **Scénario** : 1. Ajouter 1..n appareils (marque, modèle, IMEI facultatif, description, état physique, panne déclarée). 2. Valider. 3. Le système crée réparation + appareils (`RECEIVED`) + historique, génère le code. 4. Le code s'affiche en grand avec bouton « Copier ».
- **Alternatives** : aucun appareil → refus ; marque/modèle/panne vides → refus ; erreur BD → **rollback**, rien n'est créé.
- **Postconditions** : 1 réparation, n appareils, n lignes d'historique, 1 code unique.
- **Acceptation** : scénario n°3 (Youssef Amrani, iPhone 13 + Samsung S22, un seul code).

### EF-09 — Ajouter un appareil à une réparation existante
- **Préconditions** : statut global ≠ `DELIVERED` / `CANCELLED`.
- **Acceptation** : l'appareil ajouté apparaît dans le suivi avec le même code.

### EF-10 — Saisir une intervention
- **Acteur** : R (ses réparations), P
- **Préconditions** : appareil non livré / non annulé.
- **Scénario** : saisir diagnostic, pièces (texte), coût d'achat, prix client, main d'œuvre → le système copie le taux du réparateur, calcule et affiche total, commission, part propriétaire.
- **Alternatives** : montant négatif ou non numérique → refus ; prix pièces < coût → avertissement.
- **Acceptation** : scénario n°4 (300 / 400 / 100 → total 500 DH).

### EF-11 — Changer le statut d'un appareil
- **Scénario** : choisir le nouveau statut parmi **les transitions autorisées uniquement** (la liste déroulante ne montre que celles-ci) + commentaire.
- **Alternatives** : transition interdite (appel direct du service) → `BusinessException` ; livraison d'un appareil non payé → refus « Paiement requis avant livraison » (RG-18).
- **Postconditions** : statut mis à jour + historique (transaction) ; statut global recalculé.
- **Acceptation** : scénario n°9.

### EF-12 — Lister / filtrer les réparations
- **Scénario** : tableau (code, client, date, nb appareils, statut global, total, reste) ; filtres : statut, période, texte (code / nom / téléphone). Le réparateur ne voit que les siennes, le propriétaire toutes.

### EF-13 — Enregistrer un paiement
- **Acteur** : R, P (avec profil réparateur)
- **Scénario** : choisir la réparation → afficher total / payé / reste → saisir montant, mode, note → valider. Le système insère `paiement` + mouvement `ENCAISSEMENT` dans la caisse de l'utilisateur (**transaction**).
- **Alternatives** : montant ≤ 0 ; montant > reste → « Le reste à payer est de X DH » ; réparation déjà soldée → bouton désactivé.
- **Acceptation** : scénarios n°5 et n°8.

### EF-14 — Situation financière d'une réparation
- **Acceptation** : affiche total, payé, reste, statut paiement et l'historique des paiements (date, montant, mode, encaissé par).

### EF-15 — Calcul des commissions
- **Acteur** : système (`CommissionService`).
- **Acceptation** : scénario n°6 ; test unitaire avec taux 50 % puis 60 % (RG-33) ; arrondi à 2 décimales.

### EF-16 / EF-17 — Caisses
- **Scénario** : solde, liste des mouvements (date, type, montant, libellé), commission acquise / payée / due. Le propriétaire voit un tableau récapitulatif de toutes les caisses.
- **Acceptation** : après le paiement de 200 DH du scénario 5, la caisse d'Ahmed affiche +200 DH.

### EF-18 — Mouvements manuels
- **Scénario** : propriétaire → « Versement reçu » ou « Commission payée » ; réparateur → « Achat de pièce ».
- **Alternatives** : solde insuffisant ; commission payée > commission due.

### EF-19 / EF-20 — Crédits inter-boutiques
- **Scénario** : déclarer (sens, boutique, propriétaire, N° patente, téléphone, montant, date, description) ; ajouter des remboursements partiels.
- **Alternatives** : remboursement > reste dû → refus.
- **Acceptation** : scénario n°10 (1000 DH, remboursement 400 → reste 600, statut `PARTIEL`).

### EF-21 — Statistiques
- **Acteur** : P
- **Scénario** : choisir Jour / Semaine (lundi→dimanche) / Mois / Total + date de référence → indicateurs de `business-rules.md` § 9.2 + nombre de réparations créées et d'appareils livrés.
- **Règle de date** (voir `business-rules.md` § 13) : indicateurs d'activité (facturé, coûts, marges, main d'œuvre, commissions) → **appareils livrés** pendant la période ; encaissé → date du paiement ; réparations créées → date de création ; reste à encaisser et argent en caisses → situation à l'instant présent.
- **Acceptation** : avec les données des scénarios 3 à 8 : facturé 500, encaissé 500, coût pièces 300, marge pièces 100, MO 100, commissions 60, part propriétaire MO 40, marge brute 140.

### EF-23 — Suivi client
- **Acteur** : C (écran accessible depuis la page de login, sans authentification)
- **Scénario** : saisir le code + 4 derniers chiffres du téléphone (Q4 validé) → affichage des appareils et de leur statut, statut global, montants.
- **Alternatives** : code inconnu ou téléphone ne correspondant pas → même message « Aucune réparation trouvée » (on ne révèle pas lequel est faux).
- **Acceptation** : scénario n°7 ; aucun coût d'achat, commission ou nom d'utilisateur affiché.

## 3. Scénarios de démonstration (tests d'acceptation)

| # | Scénario | Exigences | Résultat attendu |
|---|----------|-----------|------------------|
| S1 | Propriétaire crée la boutique | EF-01, EF-03 | Boutique enregistrée |
| S2 | Propriétaire crée Ahmed (`ahmed`, 60 %) | EF-04 | Ahmed peut se connecter, caisse à 0 |
| S3 | Youssef Amrani dépose iPhone 13 (écran) + Samsung S22 (charge) | EF-07, EF-08 | 1 code `REP-2026-000001`, 2 appareils `RECEIVED` |
| S4 | iPhone : pièce 300 / prix 400 / MO 100 | EF-10 | Total appareil 500 DH |
| S5 | Paiement 200 DH | EF-13, EF-14 | Total 500 / payé 200 / reste 300 / `PARTIALLY_PAID` |
| S6 | Commission | EF-15 | 60 DH réparateur / 40 DH propriétaire |
| S7 | Suivi avec le code | EF-23 | iPhone `READY`, Samsung `WAITING_FOR_PART`, global `IN_PROGRESS` |
| S8 | Paiement 300 DH | EF-13 | Payé 500 / reste 0 / `PAID` |
| S9 | Les 2 appareils livrés | EF-11 | Statut global `DELIVERED` |
| S10 | Crédit 1000 DH à Boutique B, remboursement 400 | EF-19, EF-20 | Reste 600, `PARTIEL` |

> Note S5/S8 : le Samsung n'a pas encore d'intervention chiffrée, donc le total de la réparation = 500 DH. Si on chiffre le Samsung avant, le total change — l'ordre de la démo doit respecter le tableau.
