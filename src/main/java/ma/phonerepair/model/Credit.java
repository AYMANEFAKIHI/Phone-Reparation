package ma.phonerepair.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import ma.phonerepair.model.enums.SensCredit;

/**
 * Crédit entre notre boutique et une boutique externe (RG-40 à RG-44).
 */
public class Credit {

    private int id;
    private SensCredit sens;
    private String nomBoutiqueTiers;
    private String proprietaireTiers;
    private String patenteTiers;
    private String telephoneTiers;
    private BigDecimal montant;
    private LocalDate dateCredit;
    private String description;
    private int creePar;
    private List<RemboursementCredit> remboursements = new ArrayList<>();

    public Credit() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public SensCredit getSens() {
        return sens;
    }

    public void setSens(SensCredit sens) {
        this.sens = sens;
    }

    public String getNomBoutiqueTiers() {
        return nomBoutiqueTiers;
    }

    public void setNomBoutiqueTiers(String nomBoutiqueTiers) {
        this.nomBoutiqueTiers = nomBoutiqueTiers;
    }

    public String getProprietaireTiers() {
        return proprietaireTiers;
    }

    public void setProprietaireTiers(String proprietaireTiers) {
        this.proprietaireTiers = proprietaireTiers;
    }

    public String getPatenteTiers() {
        return patenteTiers;
    }

    public void setPatenteTiers(String patenteTiers) {
        this.patenteTiers = patenteTiers;
    }

    public String getTelephoneTiers() {
        return telephoneTiers;
    }

    public void setTelephoneTiers(String telephoneTiers) {
        this.telephoneTiers = telephoneTiers;
    }

    public BigDecimal getMontant() {
        return montant;
    }

    public void setMontant(BigDecimal montant) {
        this.montant = montant;
    }

    public LocalDate getDateCredit() {
        return dateCredit;
    }

    public void setDateCredit(LocalDate dateCredit) {
        this.dateCredit = dateCredit;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getCreePar() {
        return creePar;
    }

    public void setCreePar(int creePar) {
        this.creePar = creePar;
    }

    public List<RemboursementCredit> getRemboursements() {
        return remboursements;
    }

    public void setRemboursements(List<RemboursementCredit> remboursements) {
        this.remboursements = remboursements;
    }
}
