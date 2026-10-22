package ma.phonerepair.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import ma.phonerepair.model.Intervention;
import ma.phonerepair.model.Statistiques;
import ma.phonerepair.model.enums.Periode;
import org.junit.jupiter.api.Test;

/** EF-21 : indicateurs d'activité et bornes des périodes. */
class StatistiqueCalculTest {

    private static Intervention i(String cout, String prix, String mo, String taux) {
        Intervention x = new Intervention();
        x.setCoutAchatPieces(new BigDecimal(cout));
        x.setPrixPiecesClient(new BigDecimal(prix));
        x.setMainOeuvre(new BigDecimal(mo));
        x.setTauxCommissionApplique(new BigDecimal(taux));
        return x;
    }

    private static void eq(String attendu, BigDecimal valeur) {
        assertEquals(new BigDecimal(attendu), valeur);
    }

    @Test
    void valeursAttenduesDesScenarios() {
        // iPhone (300 / 400 / 100 à 60 %) + Samsung (0 DH) livrés
        Statistiques s = new Statistiques();
        StatistiqueService.calculerActivite(s, List.of(i("300", "400", "100", "60"), i("0", "0", "0", "60")));
        eq("500.00", s.getMontantFacture());
        eq("300.00", s.getCoutAchatPieces());
        eq("100.00", s.getMargePieces());
        eq("100.00", s.getMainOeuvre());
        eq("60.00", s.getCommissions());
        eq("40.00", s.getPartProprietaireMO());
        eq("140.00", s.getMargeBruteBoutique());
    }

    @Test
    void coherenceDesDefinitions() {
        Statistiques s = new Statistiques();
        StatistiqueService.calculerActivite(s, List.of(i("120", "180", "70", "70"), i("90", "80", "50", "50")));
        // facturé = coût pièces + marge pièces + main d'œuvre
        assertEquals(s.getMontantFacture(), s.getCoutAchatPieces().add(s.getMargePieces()).add(s.getMainOeuvre()));
        // main d'œuvre = commissions + part propriétaire
        assertEquals(s.getMainOeuvre(), s.getCommissions().add(s.getPartProprietaireMO()));
        eq("49.00", s.getCommissions().subtract(new BigDecimal("25.00"))); // 70 % de 70 = 49 ; 50 % de 50 = 25
    }

    @Test
    void aucuneActivite() {
        Statistiques s = new Statistiques();
        StatistiqueService.calculerActivite(s, List.of());
        eq("0.00", s.getMargeBruteBoutique());
    }

    @Test
    void bornesDesPeriodes() {
        LocalDate mercredi = LocalDate.of(2026, 10, 14);
        LocalDateTime[] j = StatistiqueService.bornes(Periode.JOUR, mercredi);
        assertEquals(LocalDateTime.of(2026, 10, 14, 0, 0), j[0]);
        assertEquals(LocalDateTime.of(2026, 10, 15, 0, 0), j[1]);
        LocalDateTime[] s = StatistiqueService.bornes(Periode.SEMAINE, mercredi);
        assertEquals(LocalDateTime.of(2026, 10, 12, 0, 0), s[0]);  // lundi
        assertEquals(LocalDateTime.of(2026, 10, 19, 0, 0), s[1]);
        LocalDateTime[] m = StatistiqueService.bornes(Periode.MOIS, mercredi);
        assertEquals(LocalDateTime.of(2026, 10, 1, 0, 0), m[0]);
        assertEquals(LocalDateTime.of(2026, 11, 1, 0, 0), m[1]);
        LocalDateTime[] t = StatistiqueService.bornes(Periode.TOTAL, mercredi);
        assertNull(t[0]);
        assertNull(t[1]);
    }
}
