package ma.phonerepair.model.enums;

/** Mode de paiement d'un client (RG-25). */
public enum TypePaiement {
    ESPECES("Espèces"),
    CARTE("Carte"),
    VIREMENT("Virement"),
    CHEQUE("Chèque");

    private final String libelle;

    TypePaiement(String libelle) {
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
