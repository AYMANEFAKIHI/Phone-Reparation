package ma.phonerepair.model;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * Chiffres d'un réparateur sur une période (EF-22) : basés sur les appareils livrés.
 * Les champs final (id, nom) n'ont pas de setter : Lombok n'en génère pas pour les champs final.
 */
@Getter
@Setter
@RequiredArgsConstructor
public class PerformanceReparateur {

    private final int reparateurId;
    private final String nomReparateur;
    private int appareilsLivres;
    private BigDecimal montantFacture = BigDecimal.ZERO.setScale(2);
    private BigDecimal mainOeuvre = BigDecimal.ZERO.setScale(2);
    private BigDecimal commission = BigDecimal.ZERO.setScale(2);
}
