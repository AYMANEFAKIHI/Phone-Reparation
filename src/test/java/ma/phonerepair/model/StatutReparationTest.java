package ma.phonerepair.model;

import static ma.phonerepair.model.enums.StatutReparation.CANCELLED;
import static ma.phonerepair.model.enums.StatutReparation.DELIVERED;
import static ma.phonerepair.model.enums.StatutReparation.DIAGNOSING;
import static ma.phonerepair.model.enums.StatutReparation.READY;
import static ma.phonerepair.model.enums.StatutReparation.RECEIVED;
import static ma.phonerepair.model.enums.StatutReparation.REPAIRING;
import static ma.phonerepair.model.enums.StatutReparation.WAITING_FOR_PART;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ma.phonerepair.model.enums.StatutReparation;
import org.junit.jupiter.api.Test;

/** RG-15 : matrice des transitions. */
class StatutReparationTest {

    @Test
    void parcoursNormal() {
        assertTrue(RECEIVED.peutPasserA(DIAGNOSING));
        assertTrue(DIAGNOSING.peutPasserA(WAITING_FOR_PART));
        assertTrue(WAITING_FOR_PART.peutPasserA(REPAIRING));
        assertTrue(REPAIRING.peutPasserA(READY));
        assertTrue(READY.peutPasserA(DELIVERED));
    }

    @Test
    void transitionsInterdites() {
        assertFalse(RECEIVED.peutPasserA(DELIVERED));
        assertFalse(RECEIVED.peutPasserA(READY));
        assertFalse(READY.peutPasserA(RECEIVED));
        assertFalse(READY.peutPasserA(CANCELLED));
        assertFalse(RECEIVED.peutPasserA(RECEIVED));
        assertFalse(RECEIVED.peutPasserA(null));
    }

    @Test
    void lesEtatsFinauxSontBloques() {
        for (StatutReparation s : StatutReparation.values()) {
            assertFalse(DELIVERED.peutPasserA(s));
            assertFalse(CANCELLED.peutPasserA(s));
        }
        assertTrue(DELIVERED.estFinal());
        assertTrue(CANCELLED.estFinal());
        assertFalse(READY.estFinal());
    }

    @Test
    void toutStatutNonFinalPeutEvoluer() {
        for (StatutReparation s : StatutReparation.values()) {
            if (!s.estFinal()) {
                assertFalse(s.suivantsAutorises().isEmpty(), s + " est bloqué");
            }
        }
    }
}
