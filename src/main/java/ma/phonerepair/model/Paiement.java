package ma.phonerepair.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import ma.phonerepair.model.enums.TypePaiement;

/**
 * Versement d'un client pour une réparation. Un paiement = une ligne (RG-25).
 */
@Getter
@Setter
@NoArgsConstructor
public class Paiement {

    private int id;
    private int reparationId;
    private BigDecimal montant;
    private LocalDateTime datePaiement;
    private TypePaiement typePaiement;
    private int utilisateurId;
    private String nomUtilisateur;
    private String note;
}
