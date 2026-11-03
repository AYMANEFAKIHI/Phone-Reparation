package ma.phonerepair.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import ma.phonerepair.model.enums.StatutGlobal;
import ma.phonerepair.model.enums.StatutReparation;

/**
 * Ligne de la liste des réparations : informations de base + montants agrégés par SQL.
 * Le statut global et la situation financière sont complétés par le service.
 */
@Getter
@Setter
@NoArgsConstructor
public class ResumeReparation {

    private int id;
    private String codeSuivi;
    private LocalDateTime dateCreation;
    private Client client;
    private int reparateurId;
    private String nomReparateur;
    private List<StatutReparation> statutsAppareils = new ArrayList<>();
    private BigDecimal total;
    private BigDecimal paye;
    private StatutGlobal statutGlobal;
    private SituationFinanciere situation;

    public int getNombreAppareils() {
        return statutsAppareils.size();
    }
}
