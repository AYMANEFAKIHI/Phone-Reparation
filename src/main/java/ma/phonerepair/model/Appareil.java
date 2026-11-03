package ma.phonerepair.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import ma.phonerepair.model.enums.StatutReparation;

/**
 * Appareil déposé dans une réparation, avec son propre statut (RG-15).
 */
@Getter
@Setter
@NoArgsConstructor
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

    public String getDesignation() {
        return marque + " " + modele;
    }
}
