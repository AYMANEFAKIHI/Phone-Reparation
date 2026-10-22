package ma.phonerepair.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Ce dont l'écran d'encaissement a besoin pour une réparation : client, situation, historique.
 * Ne contient pas les coûts d'achat (inutiles pour encaisser).
 */
public class DossierPaiement {

    private int reparationId;
    private String codeSuivi;
    private String nomClient;
    private String telephoneClient;
    private SituationFinanciere situation;
    private List<Paiement> paiements = new ArrayList<>();

    public int getReparationId() {
        return reparationId;
    }

    public void setReparationId(int reparationId) {
        this.reparationId = reparationId;
    }

    public String getCodeSuivi() {
        return codeSuivi;
    }

    public void setCodeSuivi(String codeSuivi) {
        this.codeSuivi = codeSuivi;
    }

    public String getNomClient() {
        return nomClient;
    }

    public void setNomClient(String nomClient) {
        this.nomClient = nomClient;
    }

    public String getTelephoneClient() {
        return telephoneClient;
    }

    public void setTelephoneClient(String telephoneClient) {
        this.telephoneClient = telephoneClient;
    }

    public SituationFinanciere getSituation() {
        return situation;
    }

    public void setSituation(SituationFinanciere situation) {
        this.situation = situation;
    }

    public List<Paiement> getPaiements() {
        return paiements;
    }

    public void setPaiements(List<Paiement> paiements) {
        this.paiements = paiements;
    }
}
