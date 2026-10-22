package ma.phonerepair.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import ma.phonerepair.model.enums.TypePaiement;

/**
 * Versement d'un client pour une réparation. Un paiement = une ligne (RG-25).
 */
public class Paiement {

    private int id;
    private int reparationId;
    private BigDecimal montant;
    private LocalDateTime datePaiement;
    private TypePaiement typePaiement;
    private int utilisateurId;
    private String nomUtilisateur;
    private String note;

    public Paiement() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getReparationId() {
        return reparationId;
    }

    public void setReparationId(int reparationId) {
        this.reparationId = reparationId;
    }

    public BigDecimal getMontant() {
        return montant;
    }

    public void setMontant(BigDecimal montant) {
        this.montant = montant;
    }

    public LocalDateTime getDatePaiement() {
        return datePaiement;
    }

    public void setDatePaiement(LocalDateTime datePaiement) {
        this.datePaiement = datePaiement;
    }

    public TypePaiement getTypePaiement() {
        return typePaiement;
    }

    public void setTypePaiement(TypePaiement typePaiement) {
        this.typePaiement = typePaiement;
    }

    public int getUtilisateurId() {
        return utilisateurId;
    }

    public void setUtilisateurId(int utilisateurId) {
        this.utilisateurId = utilisateurId;
    }

    public String getNomUtilisateur() {
        return nomUtilisateur;
    }

    public void setNomUtilisateur(String nomUtilisateur) {
        this.nomUtilisateur = nomUtilisateur;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
