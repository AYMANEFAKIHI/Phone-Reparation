package ma.phonerepair.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import ma.phonerepair.model.enums.StatutReparation;

/**
 * Appareil déposé dans une réparation, avec son propre statut (RG-15).
 */
public class Appareil {

    private int id;
    private int reparationId;
    private String marque;
    private String modele;
    private String imeiSerie;
    private String description;
    private String etatPhysique;
    private String problemeSignale;
    private StatutReparation statut;
    private LocalDateTime dateMajStatut;
    private List<Intervention> interventions = new ArrayList<>();

    public Appareil() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getReparationId() {
        return reparationId;
    }

    public void setReparationId(int reparationId) {
        this.reparationId = reparationId;
    }

    public String getMarque() {
        return marque;
    }

    public void setMarque(String marque) {
        this.marque = marque;
    }

    public String getModele() {
        return modele;
    }

    public void setModele(String modele) {
        this.modele = modele;
    }

    public String getImeiSerie() {
        return imeiSerie;
    }

    public void setImeiSerie(String imeiSerie) {
        this.imeiSerie = imeiSerie;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEtatPhysique() {
        return etatPhysique;
    }

    public void setEtatPhysique(String etatPhysique) {
        this.etatPhysique = etatPhysique;
    }

    public String getProblemeSignale() {
        return problemeSignale;
    }

    public void setProblemeSignale(String problemeSignale) {
        this.problemeSignale = problemeSignale;
    }

    public StatutReparation getStatut() {
        return statut;
    }

    public void setStatut(StatutReparation statut) {
        this.statut = statut;
    }

    public LocalDateTime getDateMajStatut() {
        return dateMajStatut;
    }

    public void setDateMajStatut(LocalDateTime dateMajStatut) {
        this.dateMajStatut = dateMajStatut;
    }

    public List<Intervention> getInterventions() {
        return interventions;
    }

    public void setInterventions(List<Intervention> interventions) {
        this.interventions = interventions;
    }

    public String getDesignation() {
        return marque + " " + modele;
    }
}
