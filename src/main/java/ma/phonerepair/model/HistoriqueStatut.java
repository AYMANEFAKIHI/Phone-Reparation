package ma.phonerepair.model;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import ma.phonerepair.model.enums.StatutReparation;

/**
 * Trace d'un changement de statut d'un appareil (RG-16).
 */
@Getter
@Setter
@NoArgsConstructor
public class HistoriqueStatut {

    private int id;
    private int appareilId;
    private StatutReparation ancienStatut;
    private StatutReparation nouveauStatut;
    private LocalDateTime dateChangement;
    private int utilisateurId;
    private String nomUtilisateur;
    private String commentaire;
}
