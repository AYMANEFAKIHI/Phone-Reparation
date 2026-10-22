package ma.phonerepair.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CodeGeneratorTest {

    @Test
    void formatDuSujet() {
        assertEquals("REP-2026-000123", CodeGenerator.formater(2026, 123));
        assertEquals("REP-2026-000001", CodeGenerator.formater(2026, 1));
    }

    @Test
    void numeroDepuisCode() {
        assertEquals(123, CodeGenerator.numero("REP-2026-000123"));
    }

    @Test
    void numeroHorsLimites() {
        assertThrows(IllegalArgumentException.class, () -> CodeGenerator.formater(2026, 0));
        assertThrows(IllegalArgumentException.class, () -> CodeGenerator.formater(2026, 1_000_000));
    }

    @Test
    void validationEtNormalisationDeLaSaisieClient() {
        assertTrue(CodeGenerator.estFormatValide(CodeGenerator.normaliser(" rep-2026-000123 ")));
        assertFalse(CodeGenerator.estFormatValide("REP-26-123"));
        assertFalse(CodeGenerator.estFormatValide(null));
    }

    @Test
    void lOrdreAlphabetiqueEstLOrdreNumerique() {
        // Utilisé par ReparationDAO (MAX(code_suivi)) : les zéros initiaux le garantissent.
        assertTrue(CodeGenerator.formater(2026, 99).compareTo(CodeGenerator.formater(2026, 100)) < 0);
    }
}
