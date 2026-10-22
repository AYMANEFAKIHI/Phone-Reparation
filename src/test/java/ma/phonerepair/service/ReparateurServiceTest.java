package ma.phonerepair.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import ma.phonerepair.service.exception.ValidationException;
import org.junit.jupiter.api.Test;

/** Validation du taux de commission (RG-31) — sans base de données. */
class ReparateurServiceTest {

    @Test
    void tauxValides() {
        assertEquals(new BigDecimal("60.00"), ReparateurService.validerTaux(new BigDecimal("60")));
        assertEquals(new BigDecimal("0.00"), ReparateurService.validerTaux(BigDecimal.ZERO));
        assertEquals(new BigDecimal("100.00"), ReparateurService.validerTaux(new BigDecimal("100")));
        assertEquals(new BigDecimal("33.50"), ReparateurService.validerTaux(new BigDecimal("33.5")));
    }

    @Test
    void tauxInvalides() {
        assertThrows(ValidationException.class, () -> ReparateurService.validerTaux(null));
        assertThrows(ValidationException.class, () -> ReparateurService.validerTaux(new BigDecimal("-1")));
        assertThrows(ValidationException.class, () -> ReparateurService.validerTaux(new BigDecimal("100.01")));
        assertThrows(ValidationException.class, () -> ReparateurService.validerTaux(new BigDecimal("10.555")));
    }
}
