# Diagramme d'activité — cycle d'une réparation

> Règles appliquées : RG-10 à RG-19, RG-26, RG-33, RG-18 (livraison payée).

```mermaid
flowchart TD
    A([Client arrive à la boutique]) --> B[Réparateur se connecte]
    B --> C{Client existe ?<br/>recherche par téléphone}
    C -- non --> D[Créer le client<br/>nom + téléphone]
    C -- oui --> E[Sélectionner le client]
    D --> E
    E --> F[Ajouter un appareil<br/>marque, modèle, IMEI, état, panne]
    F --> G{Autre appareil ?}
    G -- oui --> F
    G -- non --> H[Enregistrer la réparation]
    H --> I[[Système : code REP-AAAA-NNNNNN<br/>appareils = RECEIVED + historique]]
    I --> J[Remettre le code au client]

    J --> K[Pour chaque appareil : diagnostic]
    K --> L[Saisir intervention<br/>pièces : coût achat + prix client<br/>main d'œuvre]
    L --> M[[Système : taux de commission figé<br/>total recalculé]]
    M --> N{Accord du client ?}
    N -- en attente --> N1[WAITING_FOR_APPROVAL] --> N
    N -- refus --> X[CANCELLED] --> Z
    N -- accord --> O{Pièce disponible ?}
    O -- non --> O1[WAITING_FOR_PART] --> O
    O -- oui --> P[REPAIRING]
    P --> Q[READY]

    J -.-> PAY[Paiement : rien, avance ou total<br/>chaque paiement enregistré séparément]
    PAY -.-> R

    Q --> R{Payé ?<br/>payé ≥ montant des appareils livrés + celui-ci}
    R -- non --> R1[Encaisser le reste] --> R
    R -- oui --> S[DELIVERED<br/>commission acquise]
    S --> Z{Tous les appareils<br/>DELIVERED ou CANCELLED ?}
    Z -- non --> K
    Z -- oui --> FIN([Réparation livrée / clôturée])

    J -.-> SUIVI[Client : suivi avec code + 4 chiffres<br/>à tout moment]
```

### Couloirs (qui fait quoi)

| Étape | Client | Réparateur | Système |
|-------|:-:|:-:|:-:|
| Fournir nom + téléphone, déposer les appareils | ✅ | | |
| Rechercher / créer client, saisir les appareils | | ✅ | |
| Générer le code, historiser les statuts | | | ✅ |
| Diagnostic, intervention, changements de statut | | ✅ | |
| Figer le taux, calculer total / reste / statut global | | | ✅ |
| Payer | ✅ | encaisse | contrôle le reste |
| Suivre la réparation | ✅ | | filtre les informations |
