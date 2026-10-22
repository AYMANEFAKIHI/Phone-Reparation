package ma.phonerepair.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Réparation (commande) : un client, un code de suivi, un ou plusieurs appareils (RG-10, RG-11).
 */
public class Reparation {

    private int id;
    private String codeSuivi;
    private Client client;
    private int reparateurId;
    private int creePar;
    private LocalDateTime dateCreation;
    private String remarque;
    private String nomReparateur;
    private List<Appareil> appareils = new ArrayList<>();

    public Reparation() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCodeSuivi() {
        return codeSuivi;
    }

    public void setCodeSuivi(String codeSuivi) {
        this.codeSuivi = codeSuivi;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public int getReparateurId() {
        return reparateurId;
    }

    public void setReparateurId(int reparateurId) {
        this.reparateurId = reparateurId;
    }

    public int getCreePar() {
        return creePar;
    }

    public void setCreePar(int creePar) {
        this.creePar = creePar;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    public String getRemarque() {
        return remarque;
    }

    public void setRemarque(String remarque) {
        this.remarque = remarque;
    }

    public List<Appareil> getAppareils() {
        return appareils;
    }

    public void setAppareils(List<Appareil> appareils) {
        this.appareils = appareils;
    }

    public String getNomReparateur() {
        return nomReparateur;
    }

    public void setNomReparateur(String nomReparateur) {
        this.nomReparateur = nomReparateur;
    }
}
