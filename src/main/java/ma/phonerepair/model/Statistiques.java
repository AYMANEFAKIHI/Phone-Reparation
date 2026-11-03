package ma.phonerepair.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import ma.phonerepair.model.enums.Periode;

/**
 * Résultats d'une période (EF-21). Définitions exactes : business-rules.md § 9.2 et § 13.
 * Les indicateurs « activité » portent sur les appareils LIVRÉS pendant la période
 * (c'est la livraison qui rend la commission acquise, Q2) ; « encaissé » porte sur la date des paiements.
 * Aucun indicateur n'est appelé « bénéfice ».
 */
@Getter
@Setter
@NoArgsConstructor
public class Statistiques {

    private Periode periode;
    /** Bornes [debut ; fin[ — null pour TOTAL. */
    private LocalDateTime debut;
    private LocalDateTime fin;
    private int nombreReparations;
    private int nombreAppareilsLivres;
    private BigDecimal montantFacture;
    private BigDecimal totalEncaisse;
    private BigDecimal coutAchatPieces;
    private BigDecimal margePieces;
    private BigDecimal mainOeuvre;
    private BigDecimal commissions;
    private BigDecimal partProprietaireMO;
    private BigDecimal margeBruteBoutique;
    /** Instantanés (indépendants de la période). */
    private BigDecimal resteAEncaisser;
    private BigDecimal soldeCaisses;
    private BigDecimal creditsANousDus;
    private BigDecimal creditsQueNousDevons;
    private List<PerformanceReparateur> performances = new ArrayList<>();
}
