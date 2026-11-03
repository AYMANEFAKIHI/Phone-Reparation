package ma.phonerepair.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Statut de paiement d'une réparation, calculé (RG-28). Jamais stocké. */
@Getter
@RequiredArgsConstructor
public enum StatutPaiement {
    UNPAID("Non payé"),
    PARTIALLY_PAID("Partiellement payé"),
    PAID("Payé");

    private final String libelle;

    @Override
    public String toString() {
        return libelle;
    }
}
