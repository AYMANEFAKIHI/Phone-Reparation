package ma.phonerepair.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Remboursement partiel ou total d'un crédit inter-boutiques (RG-42).
 */
@Getter
@Setter
@NoArgsConstructor
public class RemboursementCredit {

    private int id;
    private int creditId;
    private BigDecimal montant;
    private LocalDate dateRemboursement;
    private String note;
    private int utilisateurId;
}
