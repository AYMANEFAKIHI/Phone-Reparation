package ma.phonerepair.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Ce dont l'écran d'encaissement a besoin pour une réparation : client, situation, historique.
 * Ne contient pas les coûts d'achat (inutiles pour encaisser).
 */
@Getter
@Setter
@NoArgsConstructor
public class DossierPaiement {

    private int reparationId;
    private String codeSuivi;
    private String nomClient;
    private String telephoneClient;
    private SituationFinanciere situation;
    private List<Paiement> paiements = new ArrayList<>();
}
