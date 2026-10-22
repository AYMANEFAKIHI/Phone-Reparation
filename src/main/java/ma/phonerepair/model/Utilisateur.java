package ma.phonerepair.model;

import java.time.LocalDateTime;

import ma.phonerepair.model.enums.Role;

/**
 * Compte de connexion (propriétaire ou réparateur). Le profil réparateur est facultatif (RG-02).
 */
public class Utilisateur {

    private int id;
    private String nomComplet;
    private String telephone;
    private String username;
    private String passwordHash;
    private Role role;
    private boolean actif = true;
    private LocalDateTime dateCreation;
    private Reparateur profilReparateur;

    public Utilisateur() {
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public boolean isActif() {
        return actif;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    public Reparateur getProfilReparateur() {
        return profilReparateur;
    }

    public void setProfilReparateur(Reparateur profilReparateur) {
        this.profilReparateur = profilReparateur;
    }

    public boolean estProprietaire() {
        return role == Role.PROPRIETAIRE;
    }

    /** Vrai si l'utilisateur peut réparer (réparateur, ou propriétaire avec profil réparateur). */
    public boolean aProfilReparateur() {
        return profilReparateur != null;
    }

    @Override
    public String toString() {
        return nomComplet + " (" + username + ")";
    }
}
