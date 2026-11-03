package ma.phonerepair.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Réparation (commande) : un client, un code de suivi, un ou plusieurs appareils (RG-10, RG-11).
 */
@Getter
@Setter
@NoArgsConstructor
public class Reparation {

    private int id;
    private String codeSuivi;
    private Client client;
    private int reparateurId;
    private String nomReparateur;
    private int creePar;
    private LocalDateTime dateCreation;
    private String remarque;
    private List<Appareil> appareils = new ArrayList<>();
}
