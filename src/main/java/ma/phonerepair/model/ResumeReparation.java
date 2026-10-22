package ma.phonerepair.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import ma.phonerepair.model.enums.StatutGlobal;
import ma.phonerepair.model.enums.StatutReparation;

/**
 * Ligne de la liste des réparations : informations de base + montants agrégés par SQL.
 * Le statut global et la situation financière sont complétés par le service.
 */
public class ResumeReparation {

    private int id;
    private String codeSuivi;
    private LocalDateTime dateCreation;
    private Client client;
    private int reparateurId;
    private String nomReparateur;
    private List<StatutReparation> statutsAppareils = new ArrayList<>();
    private BigDecimal total;
    private BigDecimal paye;
    private StatutGlobal statutGlobal;
    private SituationFinanciere situation;

    public ResumeReparation() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCodeSuivi() {
        return codeSuivi;
    }

    public void setCodeSuivi(String codeSuivi) {
        this.codeSuivi = codeSuivi;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public int getReparateurId() {
        return reparateurId;
    }

    public void setReparateurId(int reparateurId) {
        this.reparateurId = reparateurId;
    }

    public String getNomReparateur() {
        return nomReparateur;
    }

    public void setNomReparateur(String nomReparateur) {
        this.nomReparateur = nomReparateur;
    }

    public List<StatutReparation> getStatutsAppareils() {
        return statutsAppareils;
    }

    public void setStatutsAppareils(List<StatutReparation> statutsAppareils) {
        this.statutsAppareils = statutsAppareils;
    }

    public int getNombreAppareils() {
        return statutsAppareils.size();
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public BigDecimal getPaye() {
        return paye;
    }

    public void setPaye(BigDecimal paye) {
        this.paye = paye;
    }

    public StatutGlobal getStatutGlobal() {
        return statutGlobal;
    }

    public void setStatutGlobal(StatutGlobal statutGlobal) {
        this.statutGlobal = statutGlobal;
    }

    public SituationFinanciere getSituation() {
        return situation;
    }

    public void setSituation(SituationFinanciere situation) {
        this.situation = situation;
    }
}
