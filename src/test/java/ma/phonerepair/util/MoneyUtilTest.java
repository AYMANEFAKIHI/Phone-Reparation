package ma.phonerepair.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class MoneyUtilTest {

    @Test
    void commissionDuScenario6() {
        // Main d'œuvre 100 DH, taux 60 % → 60,00 DH
        assertEquals(new BigDecimal("60.00"), MoneyUtil.pourcentage(new BigDecimal("100"), new BigDecimal("60")));
    }

    @Test
    void pourcentageArrondiHalfUp() {
        // 33,33 × 50 % = 16,665 → 16,67
        assertEquals(new BigDecimal("16.67"), MoneyUtil.pourcentage(new BigDecimal("33.33"), new BigDecimal("50")));
    }

    @Test
    void parserAccepteVirguleEspacesEtSuffixe() {
        assertEquals(new BigDecimal("1250.50"), MoneyUtil.parser("1 250,50"));
        assertEquals(new BigDecimal("300.00"), MoneyUtil.parser("300 DH"));
        assertEquals(new BigDecimal("12.50"), MoneyUtil.parser("12.5"));
    }

    @Test
    void parserRefuseTexteEtTropDeDecimales() {
        assertThrows(NumberFormatException.class, () -> MoneyUtil.parser("abc"));
        assertThrows(NumberFormatException.class, () -> MoneyUtil.parser(""));
        assertThrows(NumberFormatException.class, () -> MoneyUtil.parser("10.123"));
    }

    @Test
    void formaterAvecSeparateurs() {
        assertEquals("1 250,50 DH", MoneyUtil.formater(new BigDecimal("1250.5")));
        assertEquals("0,00 DH", MoneyUtil.formater(null));
    }
}
