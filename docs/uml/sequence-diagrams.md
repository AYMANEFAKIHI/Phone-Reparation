# Diagrammes de séquence — RepairTrack

> Noms de classes et de méthodes = ceux du code (ou du contrat `class-diagram.md` pour J3–J4).
> `TM` = `TransactionManager`. Un bloc « transaction » signifie : tout est validé ensemble, ou rien (rollback).

## 1. Connexion (J1 ✅)

```mermaid
sequenceDiagram
    actor U as Utilisateur
    participant UI as LoginFrame
    participant S as AuthService
    participant D as UtilisateurDAO
    participant DB as MySQL
    U->>UI: saisit username + mot de passe
    UI->>S: connecter(username, motDePasse)
    S->>D: trouverParUsername(conn, username)
    D->>DB: SELECT ... LEFT JOIN reparateur
    DB-->>D: ligne ou rien
    D-->>S: Optional<Utilisateur>
    alt inconnu OU PasswordUtil.verifier() = faux
        S-->>UI: BusinessException("identifiants incorrects")
        UI-->>U: message d'erreur
    else compte désactivé
        S-->>UI: BusinessException("Compte désactivé")
    else OK
        S-->>UI: Utilisateur (+ profil réparateur)
        UI->>UI: new DashboardFrame(utilisateur)
        UI-->>U: menu selon le rôle
    end
    Note over S: le mot de passe (char[]) est effacé dans tous les cas
```

## 2. Créer une réparation (J2 ✅)

```mermaid
sequenceDiagram
    actor R as Réparateur
    participant UI as NouvelleReparationPanel
    participant CS as ClientService
    participant RS as ReparationService
    participant RD as ReparationDAO
    participant AD as AppareilDAO
    participant HD as HistoriqueStatutDAO
    R->>UI: tape le téléphone du client
    UI->>CS: rechercher(acteur, "0612")
    CS-->>UI: clients trouvés
    alt client inexistant
        UI->>CS: creer(acteur, nom, téléphone)
        CS-->>UI: Client
    end
    R->>UI: ajoute 1..n appareils, clique "Enregistrer"
    UI->>RS: creer(acteur, client, appareils, remarque)
    RS->>RS: exigerProfilReparateur + valider (≥ 1 appareil, champs)
    rect rgb(235, 245, 255)
    Note over RS,HD: transaction (TM.enTransaction)
    RS->>RD: prochainNumero(conn, 2026)
    RD-->>RS: 124
    RS->>RS: CodeGenerator.formater(2026, 124) = REP-2026-000124
    RS->>RD: inserer(conn, reparation)
    loop pour chaque appareil
        RS->>AD: inserer(conn, appareil RECEIVED)
        RS->>HD: inserer(conn, null → RECEIVED)
    end
    end
    alt doublon de code (création simultanée)
        RS->>RS: réessaie (max 3 fois)
    end
    RS-->>UI: Reparation (code)
    UI-->>R: affiche le code à remettre au client
```

## 3. Ajouter un appareil à une réparation existante (J2 ✅)

```mermaid
sequenceDiagram
    actor R as Réparateur
    participant UI as DetailReparationDialog
    participant RS as ReparationService
    participant RD as ReparationDAO
    participant AD as AppareilDAO
    participant HD as HistoriqueStatutDAO
    R->>UI: "Ajouter un appareil"
    UI->>RS: ajouterAppareil(acteur, reparationId, appareil)
    RS->>RD: trouverParId(conn, id)
    RS->>RS: droits (sa réparation ou propriétaire)
    RS->>RS: statut global DELIVERED ou CANCELLED ? → refus
    rect rgb(235, 245, 255)
    Note over RS,HD: transaction
    RS->>AD: inserer(conn, appareil RECEIVED)
    RS->>HD: inserer(conn, null → RECEIVED)
    end
    RS-->>UI: Appareil
    Note over UI: le code de suivi ne change pas
```

## 4. Enregistrer un paiement (J3 ✅)

```mermaid
sequenceDiagram
    actor R as Réparateur
    participant UI as PaiementPanel
    participant PS as PaiementService
    participant PD as PaiementDAO
    participant RD as ReparationDAO
    participant CD as CaisseDAO
    R->>UI: 200 DH, espèces
    UI->>PS: enregistrer(acteur, reparationId, 200, ESPECES, note)
    PS->>PS: exigerProfilReparateur, montant > 0
    rect rgb(235, 245, 255)
    Note over PS,CD: transaction
    PS->>RD: verrouiller(conn, id) — SELECT … FOR UPDATE
    PS->>RD: montantTotal(conn, id)
    PS->>PD: totalPaye(conn, id)
    PS->>PS: 200 ≤ reste ? sinon ValidationException
    PS->>PD: inserer(conn, paiement) → paiementId
    PS->>CD: trouverParReparateur(conn, profil de l'acteur)
    PS->>CD: insererMouvement(conn, caisse de l'acteur, ENCAISSEMENT, 200, paiementId)
    end
    PS-->>UI: SituationFinanciere(500, 200, 300, PARTIALLY_PAID)
```

## 5. Changer le statut d'un appareil (J2 ✅)

```mermaid
sequenceDiagram
    actor R as Réparateur
    participant UI as DetailReparationDialog
    participant AS as AppareilService
    participant AD as AppareilDAO
    participant PD as PaiementDAO
    participant HD as HistoriqueStatutDAO
    R->>UI: choisit "Livré" (seules les transitions autorisées sont proposées)
    UI->>AS: changerStatut(acteur, appareilId, DELIVERED, commentaire)
    rect rgb(235, 245, 255)
    Note over AS,HD: transaction
    AS->>AD: trouverParId(conn, appareilId)
    AS->>AS: droits + statutActuel.peutPasserA(DELIVERED) ?
    alt nouveau statut = DELIVERED (RG-18)
        AS->>AD: montantLivreOuALivrer(conn, reparationId, appareilId)
        AS->>PD: totalPaye(conn, reparationId)
        AS->>AS: payé < montant ? → BusinessException("Paiement requis… il manque X DH")
    end
    AS->>AD: modifierStatut(conn, appareilId, DELIVERED)
    AS->>HD: inserer(conn, READY → DELIVERED, acteur, commentaire)
    end
    AS-->>UI: OK
    UI->>UI: recharge → statut global recalculé (RG-19)
```

## 6. Suivi client (J2 ✅)

```mermaid
sequenceDiagram
    actor C as Client
    participant UI as SuiviReparationPanel
    participant SS as SuiviService
    participant RD as ReparationDAO
    participant AD as AppareilDAO
    participant PS as PaiementService
    C->>UI: REP-2026-000124 + "5678"
    UI->>SS: suivre(code, "5678")
    SS->>SS: format du code valide ?
    SS->>RD: trouverParCode(conn, code)
    alt introuvable OU téléphone ne finit pas par 5678
        SS-->>UI: BusinessException("Aucune réparation trouvée")
        Note over SS: même message dans les deux cas
    else trouvé
        SS->>AD: listerParReparation(conn, id)
        SS->>PS: situation(id)
        SS->>SS: construit SuiviClient (sans coûts d'achat, commission, nom d'utilisateur)
        SS-->>UI: SuiviClient
        UI-->>C: appareils + statuts + total / payé / reste
    end
```

## 7. Calculer la commission (J2–J3 ✅)

```mermaid
sequenceDiagram
    actor R as Réparateur
    participant UI as InterventionDialog
    participant AS as AppareilService
    participant RPD as ReparateurDAO
    participant ID as InterventionDAO
    participant CS as CommissionService
    R->>UI: pièce 300 / prix 400 / main d'œuvre 100
    UI->>AS: enregistrerIntervention(acteur, appareilId, intervention)
    alt première intervention de l'appareil
        AS->>RPD: trouverTaux(conn, réparateur responsable)
        RPD-->>AS: 60.00
        AS->>ID: inserer(conn, intervention, taux_commission_applique = 60.00)
    else intervention existante
        AS->>ID: modifierMontants(conn, ...) — le taux n'est PAS recopié (RG-33)
    end
    AS-->>UI: Intervention
    UI->>CS: calculerCommission(100, 60.00)
    CS-->>UI: 60.00 (part propriétaire 40.00)
    Note over RPD: si le propriétaire passe le taux à 70 % ensuite,<br/>cette intervention garde 60 %
```

Commission acquise (affichée dans « Ma caisse » et « Caisses ») :

```mermaid
sequenceDiagram
    participant CaS as CaisseService
    participant CoS as CommissionService
    participant ID as InterventionDAO
    CaS->>CoS: commissionAcquise(conn, reparateurId)
    CoS->>ID: listerPourCommission(conn, reparateurId, livrees = true)
    ID-->>CoS: interventions des appareils DELIVERED
    CoS->>CoS: Σ calculerCommission(mainOeuvre, tauxCommissionApplique)
    CoS-->>CaS: 60.00
    CaS->>CaS: commission due = acquise − Σ PAIEMENT_COMMISSION
```

## 8. Remboursement d'un crédit inter-boutiques (J4 ✅)

```mermaid
sequenceDiagram
    actor P as Propriétaire
    participant UI as CreditsPanel
    participant CS as CreditService
    participant CD as CreditDAO
    P->>UI: crédit #3, remboursement 400 DH
    UI->>CS: rembourser(acteur, 3, 400, date, note)
    CS->>CS: exigerProprietaire, montant > 0
    rect rgb(235, 245, 255)
    Note over CS,CD: transaction
    CS->>CD: verrouiller(conn, 3) — SELECT … FOR UPDATE
    CS->>CD: trouverParId(conn, 3) (avec remboursements)
    CS->>CS: date ≥ date du crédit et pas dans le futur ?
    CS->>CS: 400 ≤ resteDu ? sinon ValidationException
    CS->>CD: insererRemboursement(conn, ...)
    end
    CS-->>UI: Credit (reste 600, statut PARTIEL)
```
