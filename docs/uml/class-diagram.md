# Diagramme de classes — RepairTrack

> Correspond au code de `src/main/java/ma/phonerepair`. Les getters/setters ne sont pas représentés. Toutes les classes représentées sont implémentées (jalons J1 à J4) — leurs signatures sont un contrat pour l'équipe.

## 1. Modèle (`model`, `model.enums`)

```mermaid
classDiagram
    direction LR
    class Boutique {
        int id
        String nom
        String adresse
        String telephone
        String numeroPatente
    }
    class Utilisateur {
        int id
        String nomComplet
        String telephone
        String username
        String passwordHash
        Role role
        boolean actif
        Reparateur profilReparateur
        +estProprietaire() boolean
        +aProfilReparateur() boolean
    }
    class Reparateur {
        int id
        int utilisateurId
        BigDecimal tauxCommission
    }
    class Caisse {
        int id
        int reparateurId
    }
    class Client {
        int id
        String nomComplet
        String telephone
    }
    class Reparation {
        int id
        String codeSuivi
        Client client
        int reparateurId
        int creePar
        LocalDateTime dateCreation
        String remarque
        List~Appareil~ appareils
    }
    class Appareil {
        int id
        int reparationId
        String marque
        String modele
        String imeiSerie
        String etatPhysique
        String problemeSignale
        StatutReparation statut
        List~Intervention~ interventions
        +getDesignation() String
    }
    class Intervention {
        int id
        int appareilId
        int reparateurId
        String diagnostic
        String piecesDescription
        BigDecimal coutAchatPieces
        BigDecimal prixPiecesClient
        BigDecimal mainOeuvre
        BigDecimal tauxCommissionApplique
    }
    class HistoriqueStatut {
        StatutReparation ancienStatut
        StatutReparation nouveauStatut
        LocalDateTime dateChangement
        String commentaire
    }
    class Paiement {
        int reparationId
        BigDecimal montant
        LocalDateTime datePaiement
        TypePaiement typePaiement
        int utilisateurId
        String note
    }
    class MouvementCaisse {
        int caisseId
        TypeMouvement type
        BigDecimal montant
        Integer paiementId
    }
    class Credit {
        SensCredit sens
        String nomBoutiqueTiers
        String patenteTiers
        BigDecimal montant
        LocalDate dateCredit
        List~RemboursementCredit~ remboursements
    }
    class RemboursementCredit {
        BigDecimal montant
        LocalDate dateRemboursement
    }
    class SituationFinanciere {
        BigDecimal total
        BigDecimal paye
        BigDecimal reste
        StatutPaiement statut
    }
    class ResumeReparation {
        int id
        String codeSuivi
        Client client
        String nomReparateur
        StatutGlobal statutGlobal
        SituationFinanciere situation
    }
    class DossierPaiement {
        int reparationId
        String codeSuivi
        String nomClient
        SituationFinanciere situation
        List~Paiement~ paiements
    }
    class ResumeCaisse {
        int caisseId
        int reparateurId
        BigDecimal solde
        BigDecimal totalEncaisse
        BigDecimal commissionAcquise
        BigDecimal commissionPayee
        BigDecimal commissionPrevisionnelle
        +getCommissionDue() BigDecimal
    }
    class Statistiques {
        Periode periode
        int nombreReparations
        int nombreAppareilsLivres
        BigDecimal montantFacture
        BigDecimal totalEncaisse
        BigDecimal margePieces
        BigDecimal commissions
        BigDecimal margeBruteBoutique
        BigDecimal resteAEncaisser
        List~PerformanceReparateur~ performances
    }
    class PerformanceReparateur {
        String nomReparateur
        int appareilsLivres
        BigDecimal montantFacture
        BigDecimal commission
    }
    Statistiques "1" *-- "0..*" PerformanceReparateur
    class SuiviClient {
        String codeSuivi
        String nomClient
        StatutGlobal statutGlobal
        List~LigneSuivi~ appareils
        SituationFinanciere situation
    }

    Utilisateur "1" --> "0..1" Reparateur : profil
    Reparateur "1" --> "1" Caisse
    Reparation "*" --> "1" Client
    Reparation "1" *-- "1..*" Appareil
    Appareil "1" *-- "0..*" Intervention
    Appareil "1" --> "1..*" HistoriqueStatut
    Reparation "1" --> "0..*" Paiement
    Caisse "1" --> "0..*" MouvementCaisse
    Paiement "1" --> "1" MouvementCaisse : génère
    Credit "1" *-- "0..*" RemboursementCredit
```

### Enums

```mermaid
classDiagram
    class Role { <<enum>> PROPRIETAIRE; REPARATEUR }
    class StatutReparation {
        <<enum>>
        RECEIVED; DIAGNOSING; WAITING_FOR_APPROVAL; WAITING_FOR_PART
        REPAIRING; READY; DELIVERED; CANCELLED
        +suivantsAutorises() Set
        +peutPasserA(StatutReparation) boolean
        +estFinal() boolean
    }
    class StatutGlobal { <<enum>> RECEIVED; IN_PROGRESS; READY; DELIVERED; CANCELLED }
    class StatutPaiement { <<enum>> UNPAID; PARTIALLY_PAID; PAID }
    class TypePaiement { <<enum>> ESPECES; CARTE; VIREMENT; CHEQUE }
    class TypeMouvement {
        <<enum>>
        ENCAISSEMENT; ACHAT_PIECES; VERSEMENT_PROPRIETAIRE; PAIEMENT_COMMISSION
        +getSens() int
    }
    class SensCredit { <<enum>> ACCORDE; RECU }
    class StatutCredit { <<enum>> EN_COURS; PARTIEL; SOLDE }
    class Periode { <<enum>> JOUR; SEMAINE; MOIS; TOTAL }
```

## 2. Services (`service`)

```mermaid
classDiagram
    direction TB
    class Autorisation {
        <<utility>>
        +exigerConnecte(Utilisateur)$
        +exigerProprietaire(Utilisateur)$
        +exigerProfilReparateur(Utilisateur)$
    }
    class AuthService {
        +connecter(String, char[]) Utilisateur
        +recharger(Utilisateur) Utilisateur
        +changerMotDePasse(Utilisateur, char[], char[])
    }
    class BoutiqueService {
        +obtenir() Optional~Boutique~
        +enregistrer(Utilisateur, Boutique)
    }
    class ReparateurService {
        +listerComptes(Utilisateur) List~Utilisateur~
        +creerReparateur(Utilisateur, String, String, String, char[], BigDecimal) int
        +modifierTaux(Utilisateur, Utilisateur, BigDecimal)
        +changerActif(Utilisateur, Utilisateur, boolean)
        +reinitialiserMotDePasse(Utilisateur, Utilisateur, char[])
        +activerProfilProprietaire(Utilisateur, BigDecimal) Utilisateur
    }
    class ClientService {
        +rechercher(Utilisateur, String) List~Client~
        +creer(Utilisateur, String, String) Client
    }
    class ReparationService {
        +creer(Utilisateur, Client, List~Appareil~, String) Reparation
        +ajouterAppareil(Utilisateur, int, Appareil) Appareil
        +obtenir(Utilisateur, int) Reparation
        +lister(Utilisateur, String, StatutGlobal) List~ResumeReparation~
        +calculerStatutGlobal(Collection~StatutReparation~)$ StatutGlobal
    }
    class AppareilService {
        +enregistrerIntervention(Utilisateur, int, Intervention) Intervention
        +changerStatut(Utilisateur, int, StatutReparation, String)
        +historique(Utilisateur, int) List~HistoriqueStatut~
    }
    class SuiviService {
        +suivre(String code, String quatreChiffres) SuiviClient
    }
    class PaiementService {
        +situation(Utilisateur, int reparationId) SituationFinanciere
        +calculerSituation(BigDecimal, BigDecimal)$ SituationFinanciere
        +chargerDossier(Utilisateur, String code) DossierPaiement
        +enregistrer(Utilisateur, int, BigDecimal, TypePaiement, String) SituationFinanciere
    }
    class CommissionService {
        +calculerCommission(BigDecimal mainOeuvre, BigDecimal taux)$ BigDecimal
        +calculerPartProprietaire(BigDecimal, BigDecimal)$ BigDecimal
        +calculerMargePieces(BigDecimal, BigDecimal)$ BigDecimal
        +totalCommissions(List~Intervention~)$ BigDecimal
        ~commissionAcquise(Connection, int) BigDecimal
        ~commissionPrevisionnelle(Connection, int) BigDecimal
    }
    class CaisseService {
        +calculerSolde(Map)$ BigDecimal
        +maCaisse(Utilisateur) ResumeCaisse
        +resume(Utilisateur, int reparateurId) ResumeCaisse
        +toutes(Utilisateur) List~ResumeCaisse~
        +mouvements(Utilisateur, int reparateurId) List~MouvementCaisse~
        +enregistrerSortie(Utilisateur, int, TypeMouvement, BigDecimal, String) ResumeCaisse
    }
    class CreditService {
        +lister(Utilisateur) List~Credit~
        +creer(Utilisateur, Credit) Credit
        +rembourser(Utilisateur, int creditId, BigDecimal, LocalDate, String) Credit
        +resteDu(Credit)$ BigDecimal
        +statut(Credit)$ StatutCredit
        +totalRestant(List~Credit~, SensCredit)$ BigDecimal
    }
    class StatistiqueService {
        +calculer(Utilisateur, Periode, LocalDate) Statistiques
        +bornes(Periode, LocalDate)$ LocalDateTime[]
        +calculerActivite(Statistiques, List~Intervention~)$
    }

    ReparationService ..> PaiementService
    AppareilService ..> PaiementService : RG-18 livraison payée
    SuiviService ..> ReparationService : statut global
    SuiviService ..> PaiementService
    PaiementService ..> CaisseService : J3 mouvement ENCAISSEMENT
    CaisseService ..> CommissionService : commissions acquises
    StatistiqueService ..> CommissionService
    StatistiqueService ..> CreditService : totaux crédits
    StatistiqueService ..> CaisseService : argent en caisses
```

Tous les services appellent `Autorisation` en début de méthode sensible (non représenté pour la lisibilité).

## 3. Accès aux données (`dao`)

```mermaid
classDiagram
    class TransactionManager {
        <<utility>>
        +enTransaction(Travail~T~)$ T
        +lecture(Travail~T~)$ T
    }
    class DataAccessException {
        +estViolationContrainte() boolean
        +concerne(String) boolean
    }
    class BoutiqueDAO { +trouver(Connection) +inserer(Connection, Boutique) +modifier(Connection, Boutique) }
    class UtilisateurDAO { +trouverParUsername() +trouverParId() +listerTous() +inserer() +modifierActif() +modifierMotDePasse() }
    class ReparateurDAO { +inserer() +modifierTaux() +trouverTaux() }
    class CaisseDAO { +creer() +trouverParReparateur() +listerToutes() +verrouiller() +insererMouvement() +totauxParType() +listerMouvements() }
    class ClientDAO { +rechercher() +inserer() +trouverParId() }
    class ReparationDAO { +prochainNumero() +inserer() +trouverParId() +trouverParCode() +lister() +verrouiller() +montantTotal() }
    class AppareilDAO { +inserer() +listerParReparation() +trouverParId() +modifierStatut() +montantLivreOuALivrer() }
    class InterventionDAO { +inserer() +modifierContenu() +listerParAppareil() +listerParReparation() +listerPourCommission() }
    class HistoriqueStatutDAO { +inserer() +listerParAppareil() }
    class PaiementDAO { +totalPaye() +inserer() +listerParReparation() }
    class CreditDAO { +inserer() +insererRemboursement() +verrouiller() +trouverParId() +listerTous() }
    class StatistiqueDAO { +compterReparations() +compterAppareilsLivres() +interventionsLivrees() +appareilsLivresParReparateur() +totalEncaisse() +resteAEncaisserGlobal() +nomsReparateurs() }
    TransactionManager ..> DataAccessException
```

Chaque méthode DAO reçoit la `Connection` en premier paramètre et déclare `throws SQLException` ; c'est le service qui ouvre la transaction via `TransactionManager`.

## 4. Présentation (`presentation`)

```mermaid
classDiagram
    class LoginFrame
    class DashboardFrame { -construireMenus() -ajouterEcran(String, JComponent) }
    class AccueilPanel
    class BoutiquePanel
    class ReparateursPanel
    class ReparateurDialog
    class NouvelleReparationPanel
    class MesReparationsPanel
    class DetailReparationDialog
    class InterventionDialog
    class SuiviReparationPanel
    class PaiementPanel
    class MaCaissePanel
    class CaissesPanel
    class StatistiquesPanel
    class CreditsPanel
    class UiUtil { +executer(Component, Action)$ boolean }
    class UiTheme

    LoginFrame --> DashboardFrame
    LoginFrame --> SuiviReparationPanel : sans login
    DashboardFrame *-- AccueilPanel
    DashboardFrame *-- BoutiquePanel
    DashboardFrame *-- ReparateursPanel
    DashboardFrame *-- NouvelleReparationPanel
    DashboardFrame *-- MesReparationsPanel
    ReparateursPanel --> ReparateurDialog
    MesReparationsPanel --> DetailReparationDialog
    DetailReparationDialog --> InterventionDialog
    DetailReparationDialog --> PaiementPanel : Encaisser
    DashboardFrame *-- PaiementPanel
    DashboardFrame *-- MaCaissePanel
    DashboardFrame *-- CaissesPanel
    DashboardFrame *-- StatistiquesPanel
    DashboardFrame *-- CreditsPanel
```
