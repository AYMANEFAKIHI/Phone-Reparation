# Diagramme de cas d'utilisation — RepairTrack

> Les identifiants EF-xx renvoient à `docs/requirements.md`. Mermaid n'a pas de diagramme de cas d'utilisation natif : il est représenté par un graphe (acteurs à gauche, cas d'utilisation dans le système).

```mermaid
flowchart LR
    P([👤 Propriétaire])
    R([👤 Réparateur])
    C([👤 Client<br/>sans compte])

    subgraph SYS[RepairTrack]
        UC01((Se connecter))
        UC03((Configurer la boutique))
        UC04((Gérer les réparateurs<br/>et leur commission))
        UC06((Activer son profil réparateur))
        UC07((Rechercher / créer un client))
        UC08((Créer une réparation<br/>multi-appareils))
        UC10((Saisir une intervention))
        UC11((Changer le statut<br/>d'un appareil))
        UC12((Consulter les réparations))
        UC13((Enregistrer un paiement))
        UC16((Consulter sa caisse<br/>et sa commission))
        UC17((Consulter toutes les caisses))
        UC18((Mouvements de caisse))
        UC19((Gérer les crédits<br/>inter-boutiques))
        UC21((Consulter les statistiques))
        UC23((Suivre sa réparation<br/>code + 4 chiffres))
        GEN((Générer le code de suivi))
    end

    P --> UC01
    R --> UC01
    P --> UC03
    P --> UC04
    P --> UC06
    P --> UC17
    P --> UC18
    P --> UC19
    P --> UC21
    P --> UC12

    R --> UC07
    R --> UC08
    R --> UC10
    R --> UC11
    R --> UC12
    R --> UC13
    R --> UC16

    C --> UC23

    UC08 -. include .-> GEN
    UC08 -. include .-> UC07
    UC13 -. include .-> UC18
```

**Héritage d'acteur** : un propriétaire qui a activé son profil réparateur (EF-06) peut réaliser tous les cas d'utilisation du réparateur, avec le même compte.

| Cas | Acteur | Jalon |
|-----|--------|-------|
| Se connecter, configurer la boutique, gérer les réparateurs, profil réparateur | P, R | J1 ✅ |
| Client, réparation, intervention, statut, suivi client | R, C | J2 ✅ |
| Paiement, commission, caisses | R, P | J3 ✅ |
| Crédits, statistiques | P | J4 ✅ |
