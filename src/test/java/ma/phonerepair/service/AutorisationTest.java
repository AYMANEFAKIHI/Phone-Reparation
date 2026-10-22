package ma.phonerepair.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import ma.phonerepair.model.Reparateur;
import ma.phonerepair.model.Utilisateur;
import ma.phonerepair.model.enums.Role;
import ma.phonerepair.service.exception.AccesRefuseException;
import org.junit.jupiter.api.Test;

/** RG-05 : contrôle des droits côté service. */
class AutorisationTest {

    private static Utilisateur utilisateur(Role role, boolean profil, boolean actif) {
        Utilisateur u = new Utilisateur();
        u.setUsername(role.name().toLowerCase());
        u.setRole(role);
        u.setActif(actif);
        if (profil) {
            Reparateur r = new Reparateur();
            r.setTauxCommission(new BigDecimal("50"));
            u.setProfilReparateur(r);
        }
        return u;
    }

    @Test
    void unReparateurNAccedePasAuxFonctionsProprietaire() {
        Utilisateur ahmed = utilisateur(Role.REPARATEUR, true, true);
        assertThrows(AccesRefuseException.class, () -> Autorisation.exigerProprietaire(ahmed));
        assertDoesNotThrow(() -> Autorisation.exigerProfilReparateur(ahmed));
    }

    @Test
    void leProprietaireSansProfilNeReparePas() {
        Utilisateur owner = utilisateur(Role.PROPRIETAIRE, false, true);
        assertDoesNotThrow(() -> Autorisation.exigerProprietaire(owner));
        assertThrows(AccesRefuseException.class, () -> Autorisation.exigerProfilReparateur(owner));
    }

    @Test
    void leProprietaireAvecProfilRepare() {
        Utilisateur owner = utilisateur(Role.PROPRIETAIRE, true, true);
        assertDoesNotThrow(() -> Autorisation.exigerProfilReparateur(owner));
    }

    @Test
    void compteDesactiveOuAnonymeRefuse() {
        assertThrows(AccesRefuseException.class,
                () -> Autorisation.exigerConnecte(utilisateur(Role.REPARATEUR, true, false)));
        assertThrows(AccesRefuseException.class, () -> Autorisation.exigerConnecte(null));
    }
}
