package ma.phonerepair.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Statut global d'une réparation, calculé à partir de ses appareils (RG-19). Jamais stocké. */
@Getter
@RequiredArgsConstructor
public enum StatutGlobal {
    RECEIVED("Reçue"),
    IN_PROGRESS("En cours"),
    READY("Prête"),
    DELIVERED("Livrée"),
    CANCELLED("Annulée");

    private final String libelle;

    @Override
    public String toString() {
        return libelle;
    }
}
