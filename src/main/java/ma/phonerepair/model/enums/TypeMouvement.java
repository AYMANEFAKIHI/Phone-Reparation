package ma.phonerepair.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Type de mouvement de caisse (business-rules § 9.1).
 * Le montant stocké est toujours positif ; le sens (+1 / -1) est donné par le type.
 */
@Getter
@RequiredArgsConstructor
public enum TypeMouvement {
    ENCAISSEMENT("Encaissement client", 1),
    ACHAT_PIECES("Achat de pièces", -1),
    VERSEMENT_PROPRIETAIRE("Versement au propriétaire", -1),
    PAIEMENT_COMMISSION("Paiement de commission", -1);

    private final String libelle;
    /** +1 pour une entrée d'argent, -1 pour une sortie. */
    private final int sens;

    public boolean estEntree() {
        return sens > 0;
    }

    @Override
    public String toString() {
        return libelle;
    }
}
