package ma.phonerepair.model;

import java.time.LocalDateTime;

/**
 * Client de la boutique : pas de compte, pas de mot de passe (RG-08).
 */
public class Client {

    private int id;
    private String nomComplet;
    private String telephone;
    private LocalDateTime dateCreation;

    public Client() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNomComplet() {
        return nomComplet;
    }

    public void setNomComplet(String nomComplet) {
        this.nomComplet = nomComplet;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    @Override
    public String toString() {
        return nomComplet + " — " + telephone;
    }
}
