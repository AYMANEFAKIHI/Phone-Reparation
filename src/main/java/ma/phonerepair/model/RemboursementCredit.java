package ma.phonerepair.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Remboursement partiel ou total d'un crédit inter-boutiques (RG-42).
 */
public class RemboursementCredit {

    private int id;
    private int creditId;
    private BigDecimal montant;
    private LocalDate dateRemboursement;
    private String note;
    private int utilisateurId;

    public RemboursementCredit() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCreditId() {
        return creditId;
    }

    public void setCreditId(int creditId) {
        this.creditId = creditId;
    }

    public BigDecimal getMontant() {
        return montant;
    }

    public void setMontant(BigDecimal montant) {
        this.montant = montant;
    }

    public LocalDate getDateRemboursement() {
        return dateRemboursement;
    }

    public void setDateRemboursement(LocalDate dateRemboursement) {
        this.dateRemboursement = dateRemboursement;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public int getUtilisateurId() {
        return utilisateurId;
    }

    public void setUtilisateurId(int utilisateurId) {
        this.utilisateurId = utilisateurId;
    }
}
