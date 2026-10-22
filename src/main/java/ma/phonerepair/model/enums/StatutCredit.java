package ma.phonerepair.model.enums;

/** Statut d'un crédit inter-boutiques, calculé à partir des remboursements (RG-43). */
public enum StatutCredit {
    EN_COURS("En cours"),
    PARTIEL("Partiellement remboursé"),
    SOLDE("Soldé");

    private final String libelle;

    StatutCredit(String libelle) {
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
