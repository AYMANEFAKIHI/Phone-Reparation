# Cahier des charges — RepairTrack

> Version 0.1 — 14/10/2026 — statut : validé
> Décisions métier Q1–Q6 validées le 14/10/2026 (`business-rules.md` § 12).

## 1. Titre du projet

**RepairTrack** — Application desktop de gestion et de suivi des réparations de smartphones dans une boutique.

## 2. Contexte

Une boutique de réparation de smartphones emploie plusieurs réparateurs. Aujourd'hui le suivi se fait sur papier ou sur un cahier : on perd la trace des appareils déposés, des avances payées par les clients, des commissions dues à chaque réparateur et des crédits entre boutiques. Le client, lui, doit appeler la boutique pour savoir où en est son téléphone.

## 3. Problématique

Comment permettre à un propriétaire de boutique de :

- suivre chaque appareil déposé, de la réception à la livraison ;
- savoir exactement combien chaque client a payé et doit encore ;
- calculer de façon fiable la commission de chaque réparateur ;
- connaître l'argent présent dans la caisse de chaque réparateur ;
- suivre les crédits qu'il accorde ou reçoit d'autres boutiques ;

tout en offrant au client un moyen simple de suivre sa réparation **sans créer de compte** ?

## 4. Objectifs

| # | Objectif |
|---|----------|
| O1 | Enregistrer une réparation (commande) contenant **un ou plusieurs appareils**, avec **un seul code de suivi**. |
| O2 | Suivre le statut **de chaque appareil** et conserver l'historique des changements. |
| O3 | Séparer clairement coût d'achat des pièces, prix des pièces facturé, main d'œuvre, commission et part du propriétaire. |
| O4 | Gérer les paiements multiples / partiels et calculer automatiquement le reste à payer. |
| O5 | Calculer la commission de chaque réparateur avec un **taux figé** au moment de l'intervention. |
| O6 | Tenir une caisse par réparateur. |
| O7 | Gérer les crédits entre boutiques avec remboursements partiels. |
| O8 | Afficher des statistiques jour / semaine / mois / total. |
| O9 | Permettre au client de suivre sa réparation avec son code. |

## 5. Utilisateurs cibles

| Acteur | Accès | Description |
|--------|-------|-------------|
| **Propriétaire** | Login + mot de passe | Configure la boutique, gère les réparateurs, voit toutes les finances. Peut aussi réparer (il a alors un profil réparateur). |
| **Réparateur** | Login + mot de passe | Crée clients et réparations, saisit diagnostics / coûts, encaisse les paiements, voit sa caisse et ses commissions. |
| **Client** | Aucun compte | Fournit nom complet + téléphone. Reçoit un code de suivi et consulte l'état de sa réparation depuis l'écran « Suivi » de l'application. |

## 6. Besoins fonctionnels (résumé)

Le détail (scénarios, critères d'acceptation) est dans `requirements.md`.

1. **Authentification** et contrôle d'accès par rôle.
2. **Boutique** : nom, adresse, téléphone, N° de patente.
3. **Réparateurs** : création de compte, désactivation, taux de commission.
4. **Clients** : création, recherche par nom ou téléphone.
5. **Réparations** : création, génération automatique du code `REP-AAAA-NNNNNN`.
6. **Appareils** : plusieurs par réparation, chacun avec marque, modèle, IMEI, état physique, panne déclarée, statut.
7. **Interventions** : diagnostic, pièces, coût d'achat, prix client, main d'œuvre, taux de commission figé.
8. **Statuts** : changement contrôlé + historique.
9. **Paiements** : aucun / avance / complet / plusieurs avances ; statut calculé.
10. **Commissions** : calcul sur la main d'œuvre.
11. **Caisses** : une par réparateur, mouvements, solde.
12. **Crédits inter-boutiques** : déclaration, remboursements partiels, reste dû.
13. **Statistiques** : jour, semaine, mois, total.
14. **Suivi client** par code.

## 7. Besoins non fonctionnels

| Catégorie | Exigence |
|-----------|----------|
| Technologie | Java 17, Swing, JDBC, MySQL 8, projet Eclipse (sans Maven). **Aucun framework.** |
| Architecture | Couches `presentation → service → dao → MySQL`, packages `app, dao, model, presentation, service, util`. |
| Sécurité | Mots de passe hachés (PBKDF2, JDK), `PreparedStatement` partout, pas de mot de passe BD dans le code, contrôle des rôles côté service. |
| Fiabilité financière | `BigDecimal` + `DECIMAL(10,2)` ; opérations multi-tables dans une transaction. |
| Ergonomie | Interface Swing cohérente : login, tableau de bord, menu latéral, tableaux, formulaires, messages d'erreur compréhensibles. |
| Maintenabilité | Code lisible par un étudiant de 3e année, pas de pattern inutile, classes et méthodes courtes. |
| Traçabilité | Historique des statuts, chaque paiement / mouvement de caisse enregistre l'utilisateur et la date. |
| Collaboration | Git + GitHub, branches `main` / `develop` / `feature/*`, revue de code avant fusion. |

## 8. Contraintes

- Projet universitaire, 5 étudiants, durée limitée.
- Interdits : Spring, Hibernate/JPA, JavaFX, Jakarta EE, ORM, frameworks web.
- Structure de packages imposée.
- Application **mono-boutique** : une installation = une boutique.

## 9. Périmètre

**Inclus** : tout ce qui est listé au § 6.

**Hors périmètre (WON'T)** :

- Gestion de stock des pièces (explicitement exclue dans le sujet initial).
- Compte / login client, application mobile ou site web de suivi.
- Envoi de SMS / e-mail.
- Facturation fiscale (TVA, factures numérotées légales).
- Multi-boutiques dans une même base.
- Paie des réparateurs (salaire fixe), comptabilité générale.

**Optionnel (COULD)** : impression / export PDF d'un ticket de dépôt, export CSV des statistiques, réaffectation d'un appareil à un autre réparateur.

## 10. Hypothèses

- La boutique est configurée une seule fois par le propriétaire (premier lancement : un compte propriétaire est créé par `data.sql`).
- Les montants sont en dirhams (DH), 2 décimales.
- Le « suivi client » se fait sur le poste de la boutique (écran accessible sans login) — c'est la seule option réaliste sans site web.
- Une réparation a **un réparateur responsable** ; le taux de commission est figé sur chaque intervention.

## 11. Règles de gestion principales

Voir `business-rules.md`. Les plus structurantes :

- 1 réparation → 1..n appareils → 1 code de suivi.
- 1 appareil → 1 statut propre ; le statut global de la réparation est **calculé**.
- Paiements stockés un par un, jamais un simple booléen « payé ».
- Commission = main d'œuvre × taux figé / 100. Ne s'applique pas aux pièces.
- Crédits inter-boutiques totalement séparés des paiements clients.

## 12. Critères d'acceptation globaux

Le projet est accepté si les **10 scénarios de démonstration** de `requirements.md` § 3 fonctionnent sur MySQL, avec les montants exacts attendus, et si la démonstration finale (20 étapes) peut être déroulée sans erreur.
