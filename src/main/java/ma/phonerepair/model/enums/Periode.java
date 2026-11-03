package ma.phonerepair.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Période des statistiques (EF-21). La semaine va du lundi au dimanche. */
@Getter
@RequiredArgsConstructor
public enum Periode {
    JOUR("Jour"),
    SEMAINE("Semaine"),
    MOIS("Mois"),
    TOTAL("Total (depuis le début)");

    private final String libelle;

    @Override
    public String toString() {
        return libelle;
    }
}
