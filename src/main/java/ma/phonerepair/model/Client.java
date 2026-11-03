package ma.phonerepair.model;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Client de la boutique : pas de compte, pas de mot de passe (RG-08).
 */
@Getter
@Setter
@NoArgsConstructor
public class Client {

    private int id;
    private String nomComplet;
    private String telephone;
    private LocalDateTime dateCreation;

    @Override
    public String toString() {
        return nomComplet + " — " + telephone;
    }
}
