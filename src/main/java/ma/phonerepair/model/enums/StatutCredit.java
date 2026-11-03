package ma.phonerepair.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Statut d'un crédit inter-boutiques, calculé à partir des remboursements (RG-43). */
@Getter
@RequiredArgsConstructor
public enum StatutCredit {
    EN_COURS("En cours"),
    PARTIEL("Partiellement remboursé"),
    SOLDE("Soldé");

    private final String libelle;

    @Override
    public String toString() {
        return libelle;
    }
}
