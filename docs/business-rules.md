# Règles de gestion — RepairTrack

> Version 0.1 — 14/10/2026. Chaque règle a un identifiant `RG-xx` à citer dans le code (Javadoc) et dans les tests.
> Toutes les décisions Q1–Q6 ont été validées le 14/10/2026 (voir § 12).

## 1. Acteurs et comptes

| ID | Règle |
|----|-------|
| RG-01 | Deux rôles applicatifs : `PROPRIETAIRE` et `REPARATEUR`. Le client n'a **aucun compte**. |
| RG-02 | Un utilisateur est « réparateur » s'il possède un **profil réparateur** (table `reparateur`). Le propriétaire peut avoir son propre profil réparateur → il répare, a une caisse et un taux, **sans créer une deuxième personne**. |
| RG-03 | Seul le propriétaire crée, modifie ou désactive les comptes réparateurs. Un compte désactivé ne peut plus se connecter mais son historique est conservé (aucune suppression physique). |
| RG-04 | Le `username` est unique. Le mot de passe fait au moins 6 caractères et n'est stocké que sous forme hachée (PBKDF2 + sel). |
| RG-05 | Le contrôle des droits est fait **dans les services** (pas seulement en masquant des boutons). Voir la matrice § 11. |

## 2. Boutique

| ID | Règle |
|----|-------|
| RG-06 | Une installation gère **une seule boutique**. Champs obligatoires : nom, adresse, téléphone, N° de patente. |
| RG-07 | Téléphones et N° de patente sont des **chaînes** (zéros initiaux, tirets). Téléphone marocain : `0[5-7]` + 8 chiffres (ex. `0612345678`), ou format international `+2126…`. |

## 3. Clients

| ID | Règle |
|----|-------|
| RG-08 | Un client = nom complet + téléphone (obligatoires). |
| RG-09 | Avant création, le réparateur recherche le client par téléphone. Si un client existe déjà avec ce numéro, l'application le propose (pas de doublon involontaire). Le téléphone n'est **pas** UNIQUE en base (deux membres d'une famille peuvent partager un numéro), mais il est indexé. |

## 4. Réparation (commande) et code de suivi

| ID | Règle |
|----|-------|
| RG-10 | Une réparation appartient à **un client** et contient **au moins un appareil**. On ne peut pas enregistrer une réparation vide. |
| RG-11 | À la création, le système génère **un code de suivi unique** au format `REP-AAAA-NNNNNN` (année de création + compteur sur 6 chiffres, remis à 1 chaque année). Le code appartient à la réparation, **jamais à un appareil**. |
| RG-12 | Unicité garantie par une contrainte `UNIQUE` en base. En cas de collision (deux créations simultanées), le service régénère le code et réessaie (max 3 fois). |
| RG-13 | Une réparation a **un réparateur responsable** : celui qui la crée (réparateur, ou propriétaire ayant un profil réparateur). Toutes ses interventions sont attribuées à ce responsable et c'est **son** taux qui est figé. Le propriétaire peut consulter et modifier toutes les réparations. *(Créer une réparation « pour le compte » d'un autre réparateur : COULD, non implémenté.)* |
| RG-14 | Création réparation + appareils + historique initial = **une seule transaction**. |

## 5. Appareils et statuts

### 5.1 Statuts d'un appareil (`StatutReparation`)

| Code | Libellé affiché | Signification |
|------|-----------------|---------------|
| `RECEIVED` | Reçu | Déposé à la boutique |
| `DIAGNOSING` | En diagnostic | Recherche de la panne |
| `WAITING_FOR_APPROVAL` | En attente d'accord | Devis communiqué, on attend l'accord du client |
| `WAITING_FOR_PART` | En attente de pièce | Pièce commandée |
| `REPAIRING` | En réparation | Réparation en cours |
| `READY` | Prêt | Le client peut récupérer l'appareil |
| `DELIVERED` | Livré | Rendu au client — **état final** |
| `CANCELLED` | Annulé | Réparation abandonnée — **état final** |

### 5.2 Transitions autorisées (RG-15)

```
RECEIVED             → DIAGNOSING, REPAIRING, CANCELLED
DIAGNOSING           → WAITING_FOR_APPROVAL, WAITING_FOR_PART, REPAIRING, CANCELLED
WAITING_FOR_APPROVAL → WAITING_FOR_PART, REPAIRING, CANCELLED
WAITING_FOR_PART     → REPAIRING, CANCELLED
REPAIRING            → WAITING_FOR_PART, READY, CANCELLED
READY                → DELIVERED, REPAIRING   (reprise si défaut constaté)
DELIVERED            → (aucune)
CANCELLED            → (aucune)
```

Toute autre transition est refusée par `AppareilService` avec le message « Transition de statut non autorisée : X → Y ». La matrice est codée **une seule fois** dans l'enum `StatutReparation` (méthode `peutPasserA(StatutReparation)`).

| ID | Règle |
|----|-------|
| RG-16 | Chaque changement de statut crée une ligne dans `historique_statut` (ancien statut, nouveau statut, date, utilisateur, commentaire facultatif) — dans la même transaction que la mise à jour. |
| RG-17 | Un appareil `DELIVERED` ou `CANCELLED` n'est plus modifiable (ni statut, ni intervention). |
| RG-18 | **[VALIDÉ Q5 — 14/10]** Un appareil réparé ne peut passer à `DELIVERED` que s'il est **payé** : le total payé de la réparation doit couvrir le montant de tous les appareils déjà livrés **plus** celui qu'on livre. Sinon : « Paiement requis avant livraison : il manque X DH ». Exemple : iPhone 500 DH + Samsung 300 DH, client a payé 500 → il peut récupérer l'iPhone, pas encore le Samsung. Un appareil sans montant (0 DH) peut être livré. |

### 5.3 Statut global de la réparation (RG-19) — **calculé, jamais stocké**

Ordre d'évaluation (première règle vraie) :

| # | Condition sur les appareils | Statut global |
|---|-----------------------------|---------------|
| 1 | Tous `CANCELLED` | `CANCELLED` |
| 2 | Tous `DELIVERED` ou `CANCELLED` (au moins un `DELIVERED`) | `DELIVERED` |
| 3 | Tous `READY`, `DELIVERED` ou `CANCELLED` (au moins un `READY`) | `READY` |
| 4 | Tous `RECEIVED` | `RECEIVED` |
| 5 | Sinon | `IN_PROGRESS` |

Exemple du sujet : appareil 1 `READY` + appareil 2 `WAITING_FOR_PART` → règle 5 → `IN_PROGRESS`.
*Pourquoi calculé ?* Le stocker obligerait à le resynchroniser à chaque changement d'appareil → risque d'incohérence.

## 6. Interventions, pièces et main d'œuvre

| ID | Règle |
|----|-------|
| RG-20 | Une intervention est rattachée à un appareil. Elle contient : diagnostic, description des pièces (texte libre — **pas de gestion de stock**), **coût d'achat des pièces**, **prix des pièces facturé au client**, **main d'œuvre**, réparateur, **taux de commission appliqué**. Un appareil a en pratique une intervention ; le modèle en accepte plusieurs (ex. reprise). |
| RG-21 | Tous les montants sont ≥ 0, en `BigDecimal` à 2 décimales (arrondi `HALF_UP`). |
| RG-22 | Le prix des pièces facturé peut être inférieur au coût d'achat (geste commercial) : avertissement à l'écran, pas de blocage. |
| RG-23 | **Montant d'une intervention** = prix pièces client + main d'œuvre. |
| RG-24 | **Montant total d'une réparation** = somme des montants des interventions des appareils **non annulés**. |

## 7. Paiements clients

| ID | Règle |
|----|-------|
| RG-25 | Les paiements sont rattachés à la **réparation** (pas à un appareil). Chaque paiement est une ligne : montant, date, mode (`ESPECES`, `CARTE`, `VIREMENT`, `CHEQUE`), utilisateur qui l'a saisi, note. |
| RG-26 | Montant d'un paiement > 0 (2 décimales max) et ≤ reste à payer. Pas de trop-perçu. Conséquence : une avance n'est possible qu'une fois l'appareil **chiffré** (diagnostic + montants saisis) — c'est le devis qui fixe ce que le client doit. |
| RG-27 | **Total payé** = Σ paiements. **Reste à payer** = total réparation − total payé. |
| RG-28 | **Statut de paiement** (calculé, jamais stocké) : `UNPAID` si total payé = 0 ; `PAID` si reste = 0 et total > 0 ; sinon `PARTIALLY_PAID`. |
| RG-29 | Un paiement enregistré n'est ni modifié ni supprimé. Une erreur se corrige par le propriétaire via une annulation tracée (COULD — v2). |
| RG-30 | Enregistrer un paiement = insérer le paiement + insérer le mouvement de caisse `ENCAISSEMENT` → **une seule transaction**. |

> Cas limites (implémentés en J2) : si une intervention est modifiée à la baisse après paiement, ou si un appareil chiffré est annulé, et que le total payé dépasserait le nouveau total, l'opération est refusée (« le client a déjà payé X DH »). Le remboursement client n'est pas géré (WON'T).

## 8. Commissions

| ID | Règle |
|----|-------|
| RG-31 | Le propriétaire définit un **taux de commission** (0 à 100 %, 2 décimales) pour chaque réparateur. **[VALIDÉ Q1 — 14/10]** Ce taux est la **part du réparateur** sur la main d'œuvre ; le propriétaire garde le reste. |
| RG-32 | La commission porte **uniquement sur la main d'œuvre**, jamais sur les pièces. |
| RG-33 | À la création d'une intervention, le taux actuel du réparateur est **copié** dans `intervention.taux_commission_applique`. Changer le taux plus tard n'affecte **que les nouvelles interventions**. Modifier les montants d'une intervention existante ne recopie **pas** le taux. |
| RG-34 | Commission = main d'œuvre × taux appliqué / 100, arrondie à 2 décimales `HALF_UP`. Part propriétaire (main d'œuvre) = main d'œuvre − commission. |
| RG-35 | **[VALIDÉ Q2 — 14/10]** La commission est **acquise** quand l'appareil passe à `DELIVERED`. Avant, elle est « prévisionnelle ». Un appareil annulé ne génère aucune commission. |

Exemple : main d'œuvre 100 DH, taux 60 % → commission 60 DH, part propriétaire 40 DH.

## 9. Caisses

### 9.1 Modèle proposé

Une caisse = l'argent physiquement détenu par un réparateur. Une caisse par profil réparateur (y compris le propriétaire s'il répare), créée automatiquement avec le profil.

Le solde n'est **pas stocké** : il est calculé à partir des mouvements (une seule source de vérité).

| Type de mouvement | Sens | Créé par | Quand |
|-------------------|------|----------|-------|
| `ENCAISSEMENT` | + | automatique | à chaque paiement client |
| `ACHAT_PIECES` | − | réparateur | il paie une pièce avec l'argent de la caisse |
| `VERSEMENT_PROPRIETAIRE` | − | propriétaire | le réparateur remet l'argent au propriétaire |
| `PAIEMENT_COMMISSION` | − | propriétaire | le réparateur prend sa commission dans la caisse |

| ID | Règle |
|----|-------|
| RG-36 | **[VALIDÉ Q3 — 14/10]** Un paiement client entre dans la caisse **du réparateur connecté qui l'encaisse** (c'est lui qui a l'argent en main), pas forcément celui responsable de la réparation. Tout réparateur peut donc encaisser n'importe quelle réparation à partir de son **code** (écran Encaissement), même s'il ne la suit pas. |
| RG-37 | **Solde caisse** = Σ entrées − Σ sorties. Une sortie ne peut pas rendre le solde négatif. |
| RG-38 | **Commission due** à un réparateur = Σ commissions acquises − Σ mouvements `PAIEMENT_COMMISSION`. Un paiement de commission ne peut pas dépasser la commission due. |
| RG-39 | Les mouvements ne sont jamais modifiés ni supprimés. |

### 9.2 Concepts financiers à ne pas confondre

| Terme (code) | Définition exacte |
|--------------|-------------------|
| `montantFacture` | Σ (prix pièces client + main d'œuvre) des interventions non annulées |
| `totalEncaisse` | Σ paiements clients réellement reçus |
| `resteAEncaisser` | montant facturé − total encaissé |
| `coutAchatPieces` | Σ coûts d'achat des pièces |
| `margePieces` | Σ (prix pièces client − coût d'achat) |
| `mainOeuvre` | Σ main d'œuvre |
| `commissions` | Σ main d'œuvre × taux appliqué / 100 |
| `partProprietaireMO` | main d'œuvre − commissions |
| `margeBruteBoutique` | marge pièces + part propriétaire MO |

> On n'emploie **jamais** le mot « bénéfice » : la marge brute ne déduit ni loyer, ni électricité, ni salaires.

## 10. Crédits inter-boutiques

| ID | Règle |
|----|-------|
| RG-40 | Un crédit concerne **une autre boutique** (externe, non gérée dans l'application) : nom de la boutique, nom du propriétaire, N° de patente / magasin, téléphone. |
| RG-41 | **[VALIDÉ Q6]** Sens du crédit : `ACCORDE` (on a prêté → l'autre boutique nous doit) ou `RECU` (on a emprunté → on doit). |
| RG-42 | Montant > 0, date, description. Plusieurs remboursements partiels possibles, chacun > 0 et ≤ reste dû. |
| RG-43 | **Reste dû** = montant − Σ remboursements. Statut calculé : `EN_COURS` (rien remboursé), `PARTIEL`, `SOLDE`. |
| RG-44 | Les crédits n'ont **aucun lien** avec les paiements clients ni avec les caisses des réparateurs. Réservé au propriétaire. |

## 11. Matrice des droits (RG-05)

| Fonction | Propriétaire | Réparateur | Client (sans login) |
|----------|:-:|:-:|:-:|
| Configurer la boutique | ✅ | ❌ | ❌ |
| Gérer réparateurs / taux | ✅ | ❌ | ❌ |
| Créer client / réparation / appareils | ✅ (s'il a un profil réparateur) | ✅ | ❌ |
| Saisir intervention / changer statut | ✅ toutes | ✅ ses réparations | ❌ |
| Encaisser un paiement | ✅ (s'il a un profil réparateur) | ✅ | ❌ |
| Voir les coûts d'achat / marges | ✅ | ✅ ses réparations | ❌ |
| Voir toutes les caisses | ✅ | ❌ (la sienne) | ❌ |
| Versement / paiement de commission | ✅ | ❌ | ❌ |
| Statistiques boutique | ✅ | ❌ (ses chiffres) | ❌ |
| Crédits inter-boutiques | ✅ | ❌ | ❌ |
| Suivi par code | ✅ | ✅ | ✅ |

### Ce que voit le client (RG-45)

Code, date de dépôt, prénom/nom, liste des appareils (marque, modèle, panne déclarée, statut, date du dernier changement), statut global, **montant à payer, déjà payé, reste**.
**Jamais** : coûts d'achat, marge, commission, nom d'utilisateur des réparateurs, téléphone complet, notes internes.
**[VALIDÉ Q4 — 14/10]** Le suivi demande le **code + les 4 derniers chiffres du téléphone** du client, car un code séquentiel (`REP-2026-000123`) se devine facilement.

## 12. Points à confirmer par l'équipe

| # | Question | Décision | Impact |
|---|----------|------------------------|--------|
| Q1 | Le taux saisi est-il la part du **réparateur** ou celle du **propriétaire** ? (le sujet initial dit « le prop prend juste la commission ») | ✅ Part du **réparateur** (exemple 60 % → 60 DH au réparateur) | Calcul, libellés UI |
| Q2 | Quand la commission est-elle due ? | ✅ À la **livraison** de l'appareil | Statistiques, caisse |
| Q3 | Dans quelle caisse va un paiement ? | ✅ Caisse du **réparateur qui encaisse** | BD (`mouvement_caisse.caisse_id`) |
| Q4 | Sécurité du suivi client | ✅ Code + 4 derniers chiffres du téléphone | UI + requête |
| Q5 | Livraison possible avec reste à payer ? | ✅ **Non** — l'appareil doit être payé avant livraison (RG-18) | Service appareil |
| Q6 | Sens des crédits inter-boutiques | ✅ Les deux (`ACCORDE` / `RECU`) | BD |

## 13. Statistiques — définitions et règles de date (RG-46 à RG-49)

| ID | Règle |
|----|-------|
| RG-46 | Les indicateurs **d'activité** (montant facturé, coût et marge des pièces, main d'œuvre, commissions, part propriétaire, marge brute) portent sur les **appareils livrés pendant la période**. Raison : c'est la livraison qui rend la commission acquise (Q2) ; facturé, commissions et marges sont ainsi calculés sur la même base et restent cohérents entre eux. |
| RG-47 | **Encaissé** = paiements clients dont la date est dans la période (indépendamment de la livraison). **Réparations créées** = date de création. |
| RG-48 | **Reste à encaisser**, **argent en caisses** et **crédits restants** sont des photos de l'instant présent, quelle que soit la période choisie. |
| RG-49 | Périodes : jour = [00:00 ; 24:00[, semaine = du lundi au dimanche, mois = du 1er au dernier jour, total = depuis le début. |

Contrôles de cohérence vérifiés par les tests : facturé = coût pièces + marge pièces + main d'œuvre ; main d'œuvre = commissions + part propriétaire ; marge brute = marge pièces + part propriétaire. Les crédits inter-boutiques n'entrent dans aucun de ces indicateurs (RG-44).
