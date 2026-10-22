package ma.phonerepair.service;

import static ma.phonerepair.model.enums.StatutReparation.CANCELLED;
import static ma.phonerepair.model.enums.StatutReparation.DELIVERED;
import static ma.phonerepair.model.enums.StatutReparation.DIAGNOSING;
import static ma.phonerepair.model.enums.StatutReparation.READY;
import static ma.phonerepair.model.enums.StatutReparation.RECEIVED;
import static ma.phonerepair.model.enums.StatutReparation.WAITING_FOR_PART;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import ma.phonerepair.model.enums.StatutGlobal;
import org.junit.jupiter.api.Test;

/** RG-19 : statut global d'une réparation à partir de ses appareils. */
class StatutGlobalTest {

    private static StatutGlobal global(ma.phonerepair.model.enums.StatutReparation... s) {
        return ReparationService.calculerStatutGlobal(List.of(s));
    }

    @Test
    void exempleDuSujet() {
        // Appareil 1 prêt + appareil 2 en attente de pièce → en cours
        assertEquals(StatutGlobal.IN_PROGRESS, global(READY, WAITING_FOR_PART));
    }

    @Test
    void tousLivres() {
        assertEquals(StatutGlobal.DELIVERED, global(DELIVERED, DELIVERED));
    }

    @Test
    void livreEtAnnuleDonneLivre() {
        assertEquals(StatutGlobal.DELIVERED, global(DELIVERED, CANCELLED));
    }

    @Test
    void tousAnnules() {
        assertEquals(StatutGlobal.CANCELLED, global(CANCELLED, CANCELLED));
    }

    @Test
    void pretQuandToutEstPretOuTermine() {
        assertEquals(StatutGlobal.READY, global(READY, READY));
        assertEquals(StatutGlobal.READY, global(READY, DELIVERED));
        assertEquals(StatutGlobal.READY, global(READY, CANCELLED));
    }

    @Test
    void recuQuandRienNACommence() {
        assertEquals(StatutGlobal.RECEIVED, global(RECEIVED, RECEIVED));
        assertEquals(StatutGlobal.IN_PROGRESS, global(RECEIVED, DIAGNOSING));
    }

    @Test
    void unSeulAppareil() {
        assertEquals(StatutGlobal.IN_PROGRESS, global(DIAGNOSING));
        assertEquals(StatutGlobal.DELIVERED, global(DELIVERED));
    }
}
