package ma.phonerepair.model.enums;

/** Statut global d'une réparation, calculé à partir de ses appareils (RG-19). Jamais stocké. */
public enum StatutGlobal {
    RECEIVED("Reçue"),
    IN_PROGRESS("En cours"),
    READY("Prête"),
    DELIVERED("Livrée"),
    CANCELLED("Annulée");

    private final String libelle;

    StatutGlobal(String libelle) {
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
