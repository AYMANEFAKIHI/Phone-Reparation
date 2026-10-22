package ma.phonerepair.model.enums;

/** Statut de paiement d'une réparation, calculé (RG-28). Jamais stocké. */
public enum StatutPaiement {
    UNPAID("Non payé"),
    PARTIALLY_PAID("Partiellement payé"),
    PAID("Payé");

    private final String libelle;

    StatutPaiement(String libelle) {
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
