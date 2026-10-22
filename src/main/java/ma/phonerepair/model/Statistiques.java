package ma.phonerepair.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import ma.phonerepair.model.enums.Periode;

/**
 * Résultats d'une période (EF-21). Définitions exactes : business-rules.md § 9.2 et § 13.
 * Les indicateurs « activité » portent sur les appareils LIVRÉS pendant la période
 * (c'est la livraison qui rend la commission acquise, Q2) ; « encaissé » porte sur la date des paiements.
 * Aucun indicateur n'est appelé « bénéfice ».
 */
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

    public Periode getPeriode() {
        return periode;
    }

    public void setPeriode(Periode periode) {
        this.periode = periode;
    }

    public LocalDateTime getDebut() {
        return debut;
    }

    public void setDebut(LocalDateTime debut) {
        this.debut = debut;
    }

    public LocalDateTime getFin() {
        return fin;
    }

    public void setFin(LocalDateTime fin) {
        this.fin = fin;
    }

    public int getNombreReparations() {
        return nombreReparations;
    }

    public void setNombreReparations(int nombreReparations) {
        this.nombreReparations = nombreReparations;
    }

    public int getNombreAppareilsLivres() {
        return nombreAppareilsLivres;
    }

    public void setNombreAppareilsLivres(int nombreAppareilsLivres) {
        this.nombreAppareilsLivres = nombreAppareilsLivres;
    }

    public BigDecimal getMontantFacture() {
        return montantFacture;
    }

    public void setMontantFacture(BigDecimal montantFacture) {
        this.montantFacture = montantFacture;
    }

    public BigDecimal getTotalEncaisse() {
        return totalEncaisse;
    }

    public void setTotalEncaisse(BigDecimal totalEncaisse) {
        this.totalEncaisse = totalEncaisse;
    }

    public BigDecimal getCoutAchatPieces() {
        return coutAchatPieces;
    }

    public void setCoutAchatPieces(BigDecimal coutAchatPieces) {
        this.coutAchatPieces = coutAchatPieces;
    }

    public BigDecimal getMargePieces() {
        return margePieces;
    }

    public void setMargePieces(BigDecimal margePieces) {
        this.margePieces = margePieces;
    }

    public BigDecimal getMainOeuvre() {
        return mainOeuvre;
    }

    public void setMainOeuvre(BigDecimal mainOeuvre) {
        this.mainOeuvre = mainOeuvre;
    }

    public BigDecimal getCommissions() {
        return commissions;
    }

    public void setCommissions(BigDecimal commissions) {
        this.commissions = commissions;
    }

    public BigDecimal getPartProprietaireMO() {
        return partProprietaireMO;
    }

    public void setPartProprietaireMO(BigDecimal partProprietaireMO) {
        this.partProprietaireMO = partProprietaireMO;
    }

    public BigDecimal getMargeBruteBoutique() {
        return margeBruteBoutique;
    }

    public void setMargeBruteBoutique(BigDecimal margeBruteBoutique) {
        this.margeBruteBoutique = margeBruteBoutique;
    }

    public BigDecimal getResteAEncaisser() {
        return resteAEncaisser;
    }

    public void setResteAEncaisser(BigDecimal resteAEncaisser) {
        this.resteAEncaisser = resteAEncaisser;
    }

    public BigDecimal getSoldeCaisses() {
        return soldeCaisses;
    }

    public void setSoldeCaisses(BigDecimal soldeCaisses) {
        this.soldeCaisses = soldeCaisses;
    }

    public BigDecimal getCreditsANousDus() {
        return creditsANousDus;
    }

    public void setCreditsANousDus(BigDecimal creditsANousDus) {
        this.creditsANousDus = creditsANousDus;
    }

    public BigDecimal getCreditsQueNousDevons() {
        return creditsQueNousDevons;
    }

    public void setCreditsQueNousDevons(BigDecimal creditsQueNousDevons) {
        this.creditsQueNousDevons = creditsQueNousDevons;
    }

    public List<PerformanceReparateur> getPerformances() {
        return performances;
    }

    public void setPerformances(List<PerformanceReparateur> performances) {
        this.performances = performances;
    }
}
