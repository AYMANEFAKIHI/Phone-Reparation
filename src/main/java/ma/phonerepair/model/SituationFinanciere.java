package ma.phonerepair.model;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;

import ma.phonerepair.model.enums.StatutPaiement;

/**
 * Situation de paiement d'une réparation à un instant donné (RG-27, RG-28).
 * Objet en lecture seule, calculé par PaiementService — jamais stocké en base.
 */
@Getter
@AllArgsConstructor
public class SituationFinanciere {

    private final BigDecimal total;
    private final BigDecimal paye;
    private final BigDecimal reste;
    private final StatutPaiement statut;
}
