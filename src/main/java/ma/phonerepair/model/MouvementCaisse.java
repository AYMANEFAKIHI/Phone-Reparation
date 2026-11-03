package ma.phonerepair.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import ma.phonerepair.model.enums.TypeMouvement;

/**
 * Entrée ou sortie d'argent dans une caisse. Montant toujours positif, sens donné par le type.
 */
@Getter
@Setter
@NoArgsConstructor
public class MouvementCaisse {

    private int id;
    private int caisseId;
    private TypeMouvement type;
    private BigDecimal montant;
    private LocalDateTime dateMouvement;
    private Integer paiementId;
    private int utilisateurId;
    private String libelle;
    private String nomUtilisateur;

    /** Montant signé : positif pour une entrée, négatif pour une sortie. */
    public BigDecimal getMontantSigne() {
        return type.estEntree() ? montant : montant.negate();
    }
}
