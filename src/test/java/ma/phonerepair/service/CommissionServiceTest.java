package ma.phonerepair.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

/** Scénario 6 et RG-34 : commission sur la main d'œuvre uniquement. */
class CommissionServiceTest {

    private static final BigDecimal MO = new BigDecimal("100");

    @Test
    void scenario6() {
        assertEquals(new BigDecimal("60.00"), CommissionService.calculerCommission(MO, new BigDecimal("60")));
        assertEquals(new BigDecimal("40.00"), CommissionService.calculerPartProprietaire(MO, new BigDecimal("60")));
    }

    @Test
    void lesPiecesNeSontPasCommissionnees() {
        // Pièce 300 → 400, main d'œuvre 100 : la commission ne dépend que des 100 DH
        assertEquals(new BigDecimal("100.00"),
                CommissionService.calculerMargePieces(new BigDecimal("400"), new BigDecimal("300")));
        assertEquals(new BigDecimal("60.00"), CommissionService.calculerCommission(MO, new BigDecimal("60")));
    }

    @Test
    void tauxFigeAncienneEtNouvelleIntervention() {
        // Ancienne intervention à 50 %, nouveau taux 60 % : chaque intervention utilise SON taux enregistré
        assertEquals(new BigDecimal("50.00"), CommissionService.calculerCommission(MO, new BigDecimal("50.00")));
        assertEquals(new BigDecimal("60.00"), CommissionService.calculerCommission(MO, new BigDecimal("60.00")));
    }

    @Test
    void bornes() {
        assertEquals(new BigDecimal("0.00"), CommissionService.calculerCommission(MO, BigDecimal.ZERO));
        assertEquals(new BigDecimal("0.00"), CommissionService.calculerPartProprietaire(MO, new BigDecimal("100")));
    }

    @Test
    void margeNegativeAutorisee() {
        assertEquals(new BigDecimal("-20.00"),
                CommissionService.calculerMargePieces(new BigDecimal("80"), new BigDecimal("100")));
    }
}
