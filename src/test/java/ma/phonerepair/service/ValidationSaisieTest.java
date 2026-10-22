package ma.phonerepair.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import ma.phonerepair.model.Appareil;
import ma.phonerepair.model.Intervention;
import ma.phonerepair.service.exception.ValidationException;
import org.junit.jupiter.api.Test;

/** Validation des appareils et des interventions (sans base). */
class ValidationSaisieTest {

    private static Appareil appareil(String marque, String modele, String panne, String imei) {
        Appareil a = new Appareil();
        a.setMarque(marque);
        a.setModele(modele);
        a.setProblemeSignale(panne);
        a.setImeiSerie(imei);
        return a;
    }

    @Test
    void appareilValide() {
        assertDoesNotThrow(() -> ReparationService.validerAppareil(appareil("Apple", "iPhone 13", "Écran cassé", null)));
        assertDoesNotThrow(() -> ReparationService.validerAppareil(
                appareil("Samsung", "S22", "Ne charge plus", "356938035643809")));
    }

    @Test
    void appareilIncomplet() {
        assertThrows(ValidationException.class, () -> ReparationService.validerAppareil(appareil("", "X", "p", null)));
        assertThrows(ValidationException.class, () -> ReparationService.validerAppareil(appareil("A", "X", " ", null)));
        assertThrows(ValidationException.class,
                () -> ReparationService.validerAppareil(appareil("A", "X", "p", "35 69 38")));
    }

    @Test
    void interventionScenario4() {
        Intervention s = new Intervention();
        s.setDiagnostic("Écran LCD fissuré");
        s.setCoutAchatPieces(new BigDecimal("300"));
        s.setPrixPiecesClient(new BigDecimal("400"));
        s.setMainOeuvre(new BigDecimal("100"));
        Intervention i = AppareilService.validerIntervention(s);
        assertEquals(new BigDecimal("500.00"), i.getMontantClient());
    }

    @Test
    void interventionMontantNegatifRefuse() {
        Intervention s = new Intervention();
        s.setDiagnostic("x");
        s.setMainOeuvre(new BigDecimal("-1"));
        assertThrows(ValidationException.class, () -> AppareilService.validerIntervention(s));
    }

    @Test
    void interventionSansDiagnosticRefusee() {
        assertThrows(ValidationException.class, () -> AppareilService.validerIntervention(new Intervention()));
    }

    @Test
    void montantsAbsentsValentZero() {
        Intervention s = new Intervention();
        s.setDiagnostic("Nettoyage connecteur");
        s.setMainOeuvre(new BigDecimal("50"));
        Intervention i = AppareilService.validerIntervention(s);
        assertEquals(new BigDecimal("0.00"), i.getCoutAchatPieces());
        assertEquals(new BigDecimal("50.00"), i.getMontantClient());
    }
}
