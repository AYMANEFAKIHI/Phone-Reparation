package ma.phonerepair.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import ma.phonerepair.model.enums.SensCredit;

/**
 * Crédit entre notre boutique et une boutique externe (RG-40 à RG-44).
 */
@Getter
@Setter
@NoArgsConstructor
public class Credit {

    private int id;
    private SensCredit sens;
    private String nomBoutiqueTiers;
    private String proprietaireTiers;
    private String patenteTiers;
    private String telephoneTiers;
    private BigDecimal montant;
    private LocalDate dateCredit;
    private String description;
    private int creePar;
    private List<RemboursementCredit> remboursements = new ArrayList<>();
}
