package ma.phonerepair.model.enums;

/** Rôle applicatif d'un utilisateur (RG-01). */
public enum Role {
    PROPRIETAIRE("Propriétaire"),
    REPARATEUR("Réparateur");

    private final String libelle;

    Role(String libelle) {
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
