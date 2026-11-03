package ma.phonerepair.model;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Profil réparateur d'un utilisateur : taux de commission actuel (RG-31).
 */
@Getter
@Setter
@NoArgsConstructor
public class Reparateur {

    private int id;
    private int utilisateurId;
    private BigDecimal tauxCommission;
}
