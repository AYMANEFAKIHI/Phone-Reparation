package ma.phonerepair.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import ma.phonerepair.model.enums.TypeMouvement;

/**
 * Entrée ou sortie d'argent dans une caisse. Montant toujours positif, sens donné par le type.
 */
public class MouvementCaisse {

    private int id;
    private int caisseId;
    private TypeMouvement type;
    private BigDecimal montant;
    private LocalDateTime dateMouvement;
    private Integer paiementId;
    private int utilisateurId;
    private String libelle;
    private String nomUtilisateur;

    public MouvementCaisse() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCaisseId() {
        return caisseId;
    }

    public void setCaisseId(int caisseId) {
        this.caisseId = caisseId;
    }

    public TypeMouvement getType() {
        return type;
    }

    public void setType(TypeMouvement type) {
        this.type = type;
    }

    public BigDecimal getMontant() {
        return montant;
    }

    public void setMontant(BigDecimal montant) {
        this.montant = montant;
    }

    public LocalDateTime getDateMouvement() {
        return dateMouvement;
    }

    public void setDateMouvement(LocalDateTime dateMouvement) {
        this.dateMouvement = dateMouvement;
    }

    public Integer getPaiementId() {
        return paiementId;
    }

    public void setPaiementId(Integer paiementId) {
        this.paiementId = paiementId;
    }

    public int getUtilisateurId() {
        return utilisateurId;
    }

    public void setUtilisateurId(int utilisateurId) {
        this.utilisateurId = utilisateurId;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public String getNomUtilisateur() {
        return nomUtilisateur;
    }

    public void setNomUtilisateur(String nomUtilisateur) {
        this.nomUtilisateur = nomUtilisateur;
    }

    /** Montant signé : positif pour une entrée, négatif pour une sortie. */
    public BigDecimal getMontantSigne() {
        return type.estEntree() ? montant : montant.negate();
    }
}
