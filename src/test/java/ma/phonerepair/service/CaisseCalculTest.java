package ma.phonerepair.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import ma.phonerepair.model.Intervention;
import ma.phonerepair.model.enums.TypeMouvement;
import org.junit.jupiter.api.Test;

/** Solde de caisse (RG-37) et somme des commissions avec taux figés (RG-33, RG-35). */
class CaisseCalculTest {

    private static Map<TypeMouvement, BigDecimal> totaux(String enc, String achat, String verse, String comm) {
        Map<TypeMouvement, BigDecimal> m = new EnumMap<>(TypeMouvement.class);
        m.put(TypeMouvement.ENCAISSEMENT, new BigDecimal(enc));
        m.put(TypeMouvement.ACHAT_PIECES, new BigDecimal(achat));
        m.put(TypeMouvement.VERSEMENT_PROPRIETAIRE, new BigDecimal(verse));
        m.put(TypeMouvement.PAIEMENT_COMMISSION, new BigDecimal(comm));
        return m;
    }

    @Test
    void soldeApresScenarios5Et8() {
        // 200 + 300 encaissés, rien n'est sorti
        assertEquals(new BigDecimal("500.00"), CaisseService.calculerSolde(totaux("500", "0", "0", "0")));
    }

    @Test
    void soldeAvecSorties() {
        // 500 encaissés − 120 pièce − 300 versés au propriétaire − 60 commission = 20
        assertEquals(new BigDecimal("20.00"), CaisseService.calculerSolde(totaux("500", "120", "300", "60")));
    }

    @Test
    void caisseVide() {
        assertEquals(new BigDecimal("0.00"), CaisseService.calculerSolde(totaux("0", "0", "0", "0")));
    }

    private static Intervention intervention(String mo, String taux) {
        Intervention i = new Intervention();
        i.setMainOeuvre(new BigDecimal(mo));
        i.setTauxCommissionApplique(new BigDecimal(taux));
        return i;
    }

    @Test
    void chaqueInterventionGardeSonTaux() {
        // 100 DH à 50 % (ancien taux) + 100 DH à 60 % (nouveau taux) = 50 + 60
        assertEquals(new BigDecimal("110.00"),
                CommissionService.totalCommissions(List.of(intervention("100", "50"), intervention("100", "60"))));
    }

    @Test
    void aucuneIntervention() {
        assertEquals(new BigDecimal("0.00"), CommissionService.totalCommissions(List.of()));
    }
}
