package ma.phonerepair.model;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Informations de la boutique (une seule par installation — RG-06).
 */
@Getter
@Setter
@NoArgsConstructor
public class Boutique {

    private int id;
    private String nom;
    private String adresse;
    private String telephone;
    private String numeroPatente;
    private LocalDateTime dateCreation;
}
