package ma.phonerepair.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import ma.phonerepair.model.enums.StatutGlobal;
import ma.phonerepair.model.enums.StatutReparation;

/**
 * Ce que le client voit avec son code de suivi (RG-45).
 * Ne contient volontairement AUCUNE information interne : pas de coût d'achat,
 * pas de commission, pas de nom d'utilisateur, pas de téléphone complet.
 */
public class SuiviClient {

    /** Un appareil tel que le client le voit. */
    public static class LigneSuivi {
        private final String designation;
        private final String problemeSignale;
        private final StatutReparation statut;
        private final LocalDateTime dateMajStatut;

        public LigneSuivi(String designation, String problemeSignale, StatutReparation statut,
                          LocalDateTime dateMajStatut) {
            this.designation = designation;
            this.problemeSignale = problemeSignale;
            this.statut = statut;
            this.dateMajStatut = dateMajStatut;
        }

        public String getDesignation() {
            return designation;
        }

        public String getProblemeSignale() {
            return problemeSignale;
        }

        public StatutReparation getStatut() {
            return statut;
        }

        public LocalDateTime getDateMajStatut() {
            return dateMajStatut;
        }
    }

    private String codeSuivi;
    private String nomClient;
    private LocalDateTime dateDepot;
    private StatutGlobal statutGlobal;
    private List<LigneSuivi> appareils = new ArrayList<>();
    private SituationFinanciere situation;

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

    public LocalDateTime getDateDepot() {
        return dateDepot;
    }

    public void setDateDepot(LocalDateTime dateDepot) {
        this.dateDepot = dateDepot;
    }

    public StatutGlobal getStatutGlobal() {
        return statutGlobal;
    }

    public void setStatutGlobal(StatutGlobal statutGlobal) {
        this.statutGlobal = statutGlobal;
    }

    public List<LigneSuivi> getAppareils() {
        return appareils;
    }

    public void setAppareils(List<LigneSuivi> appareils) {
        this.appareils = appareils;
    }

    public SituationFinanciere getSituation() {
        return situation;
    }

    public void setSituation(SituationFinanciere situation) {
        this.situation = situation;
    }
}
