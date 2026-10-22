package ma.phonerepair.model.enums;

import java.util.EnumSet;
import java.util.Set;

/**
 * Statut d'un appareil (RG-15). La matrice des transitions est définie ici, une seule fois.
 */
public enum StatutReparation {
    RECEIVED("Reçu"),
    DIAGNOSING("En diagnostic"),
    WAITING_FOR_APPROVAL("En attente d'accord"),
    WAITING_FOR_PART("En attente de pièce"),
    REPAIRING("En réparation"),
    READY("Prêt"),
    DELIVERED("Livré"),
    CANCELLED("Annulé");

    private final String libelle;

    StatutReparation(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }

    /** Statuts vers lesquels on peut passer depuis ce statut. */
    public Set<StatutReparation> suivantsAutorises() {
        return switch (this) {
            case RECEIVED -> EnumSet.of(DIAGNOSING, REPAIRING, CANCELLED);
            case DIAGNOSING -> EnumSet.of(WAITING_FOR_APPROVAL, WAITING_FOR_PART, REPAIRING, CANCELLED);
            case WAITING_FOR_APPROVAL -> EnumSet.of(WAITING_FOR_PART, REPAIRING, CANCELLED);
            case WAITING_FOR_PART -> EnumSet.of(REPAIRING, CANCELLED);
            case REPAIRING -> EnumSet.of(WAITING_FOR_PART, READY, CANCELLED);
            case READY -> EnumSet.of(DELIVERED, REPAIRING);
            case DELIVERED, CANCELLED -> EnumSet.noneOf(StatutReparation.class);
        };
    }

    public boolean peutPasserA(StatutReparation cible) {
        return cible != null && suivantsAutorises().contains(cible);
    }

    /** DELIVERED et CANCELLED : plus aucune modification possible (RG-17). */
    public boolean estFinal() {
        return this == DELIVERED || this == CANCELLED;
    }

    @Override
    public String toString() {
        return libelle;
    }
}
