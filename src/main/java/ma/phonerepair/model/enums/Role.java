package ma.phonerepair.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Rôle applicatif d'un utilisateur (RG-01). */
@Getter
@RequiredArgsConstructor
public enum Role {
    PROPRIETAIRE("Propriétaire"),
    REPARATEUR("Réparateur");

    private final String libelle;

    @Override
    public String toString() {
        return libelle;
    }
}
