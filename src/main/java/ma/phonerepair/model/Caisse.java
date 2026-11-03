package ma.phonerepair.model;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Caisse d'un réparateur. Le solde n'est pas stocké : il est calculé à partir des mouvements (RG-37).
 */
@Getter
@Setter
@NoArgsConstructor
public class Caisse {

    private int id;
    private int reparateurId;
    private LocalDateTime dateCreation;
}
