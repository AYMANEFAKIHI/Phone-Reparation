package ma.phonerepair.model.enums;

/** Période des statistiques (EF-21). La semaine va du lundi au dimanche. */
public enum Periode {
    JOUR("Jour"),
    SEMAINE("Semaine"),
    MOIS("Mois"),
    TOTAL("Total (depuis le début)");

    private final String libelle;

    Periode(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }

    @Override
    public String toString() {
        return libelle;
    }
}
