package ma.phonerepair.model.enums;

/** Sens d'un crédit inter-boutiques (RG-41). */
public enum SensCredit {
    /** Nous avons prêté : l'autre boutique nous doit de l'argent. */
    ACCORDE("Accordé (on nous doit)"),
    /** Nous avons emprunté : nous devons de l'argent à l'autre boutique. */
    RECU("Reçu (nous devons)");

    private final String libelle;

    SensCredit(String libelle) {
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
