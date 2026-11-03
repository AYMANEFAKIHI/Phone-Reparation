package ma.phonerepair.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Mode de paiement d'un client (RG-25). */
@Getter
@RequiredArgsConstructor
public enum TypePaiement {
    ESPECES("Espèces"),
    CARTE("Carte"),
    VIREMENT("Virement"),
    CHEQUE("Chèque");

    private final String libelle;

    @Override
    public String toString() {
        return libelle;
    }
}
