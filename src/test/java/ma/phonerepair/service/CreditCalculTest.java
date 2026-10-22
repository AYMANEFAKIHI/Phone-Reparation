package ma.phonerepair.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import ma.phonerepair.model.Credit;
import ma.phonerepair.model.RemboursementCredit;
import ma.phonerepair.model.enums.SensCredit;
import ma.phonerepair.model.enums.StatutCredit;
import org.junit.jupiter.api.Test;

/** RG-43 : reste dû et statut d'un crédit inter-boutiques (scénario S10). */
class CreditCalculTest {

    private static Credit credit(SensCredit sens, String montant, String... remboursements) {
        Credit c = new Credit();
        c.setSens(sens);
        c.setMontant(new BigDecimal(montant));
        c.setDateCredit(LocalDate.of(2026, 10, 1));
        for (String r : remboursements) {
            RemboursementCredit rc = new RemboursementCredit();
            rc.setMontant(new BigDecimal(r));
            c.getRemboursements().add(rc);
        }
        return c;
    }

    @Test
    void scenario10RemboursementPartiel() {
        Credit c = credit(SensCredit.ACCORDE, "1000", "400");
        assertEquals(new BigDecimal("600.00"), CreditService.resteDu(c));
        assertEquals(StatutCredit.PARTIEL, CreditService.statut(c));
    }

    @Test
    void creditSansRemboursement() {
        assertEquals(StatutCredit.EN_COURS, CreditService.statut(credit(SensCredit.ACCORDE, "1000")));
    }

    @Test
    void creditSolde() {
        Credit c = credit(SensCredit.RECU, "1000", "400", "350", "250");
        assertEquals(new BigDecimal("0.00"), CreditService.resteDu(c));
        assertEquals(StatutCredit.SOLDE, CreditService.statut(c));
    }

    @Test
    void totauxParSens() {
        List<Credit> l = List.of(credit(SensCredit.ACCORDE, "1000", "400"), credit(SensCredit.ACCORDE, "200"),
                credit(SensCredit.RECU, "500", "100"));
        assertEquals(new BigDecimal("800.00"), CreditService.totalRestant(l, SensCredit.ACCORDE));
        assertEquals(new BigDecimal("400.00"), CreditService.totalRestant(l, SensCredit.RECU));
    }
}
