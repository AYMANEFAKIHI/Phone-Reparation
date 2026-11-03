package ma.phonerepair.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import ma.phonerepair.model.enums.StatutGlobal;
import ma.phonerepair.model.enums.StatutReparation;

/**
 * Ce que le client voit avec son code de suivi (RG-45).
 * Ne contient volontairement AUCUNE information interne : pas de coût d'achat,
 * pas de commission, pas de nom d'utilisateur, pas de téléphone complet.
 */
@Getter
@Setter
@NoArgsConstructor
public class SuiviClient {

    /** Un appareil tel que le client le voit. */
    @Getter
    @AllArgsConstructor
    public static class LigneSuivi {
        private final String designation;
        private final String problemeSignale;
        private final StatutReparation statut;
        private final LocalDateTime dateMajStatut;
    }

    private String codeSuivi;
    private String nomClient;
    private LocalDateTime dateDepot;
    private StatutGlobal statutGlobal;
    private List<LigneSuivi> appareils = new ArrayList<>();
    private SituationFinanciere situation;
}
