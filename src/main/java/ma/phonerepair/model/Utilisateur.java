package ma.phonerepair.model;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import ma.phonerepair.model.enums.Role;

/**
 * Compte de connexion (propriétaire ou réparateur). Le profil réparateur est facultatif (RG-02).
 */
@Getter
@Setter
@NoArgsConstructor
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
