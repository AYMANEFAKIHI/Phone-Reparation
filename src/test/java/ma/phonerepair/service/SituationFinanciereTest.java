package ma.phonerepair.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import ma.phonerepair.model.SituationFinanciere;
import ma.phonerepair.model.enums.StatutPaiement;
import org.junit.jupiter.api.Test;

/** RG-27, RG-28 : reste à payer et statut de paiement. */
class SituationFinanciereTest {

    private static SituationFinanciere s(String total, String paye) {
        return PaiementService.calculerSituation(new BigDecimal(total), new BigDecimal(paye));
    }

    @Test
    void scenario5PaiementPartiel() {
        SituationFinanciere sf = s("500", "200");
        assertEquals(new BigDecimal("300.00"), sf.getReste());
        assertEquals(StatutPaiement.PARTIALLY_PAID, sf.getStatut());
    }

    @Test
    void scenario8PaiementComplet() {
        SituationFinanciere sf = s("500", "500");
        assertEquals(new BigDecimal("0.00"), sf.getReste());
        assertEquals(StatutPaiement.PAID, sf.getStatut());
    }

    @Test
    void exempleTroisPaiementsDuSujet() {
        // 200 + 300 + 500 = 1000 sur un total de 1000
        SituationFinanciere sf = s("1000", "1000.00");
        assertEquals(StatutPaiement.PAID, sf.getStatut());
    }

    @Test
    void rienPaye() {
        assertEquals(StatutPaiement.UNPAID, s("500", "0").getStatut());
        assertEquals(StatutPaiement.UNPAID, s("0", "0").getStatut());
    }

    @Test
    void avanceAvantChiffrage() {
        // Avance versée alors qu'aucune intervention n'est encore chiffrée (total 0)
        assertEquals(StatutPaiement.PARTIALLY_PAID, s("0", "100").getStatut());
    }
}
