package ma.phonerepair.model;

import java.math.BigDecimal;

/**
 * État de la caisse d'un réparateur (business-rules § 9). Tout est calculé, rien n'est stocké.
 * <ul>
 *   <li>solde = entrées − sorties (argent physiquement détenu)</li>
 *   <li>commission acquise = commissions des appareils livrés</li>
 *   <li>commission due = acquise − déjà payée</li>
 *   <li>commission prévisionnelle = appareils chiffrés pas encore livrés</li>
 * </ul>
 */
public class ResumeCaisse {

    private int caisseId;
    private int reparateurId;
    private String nomReparateur;
    private boolean actif;
    private BigDecimal solde;
    private BigDecimal totalEncaisse;
    private BigDecimal totalAchatsPieces;
    private BigDecimal totalVerse;
    private BigDecimal commissionAcquise;
    private BigDecimal commissionPayee;
    private BigDecimal commissionPrevisionnelle;

    public int getCaisseId() {
        return caisseId;
    }

    public void setCaisseId(int caisseId) {
        this.caisseId = caisseId;
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

    public boolean isActif() {
        return actif;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }

    public BigDecimal getSolde() {
        return solde;
    }

    public void setSolde(BigDecimal solde) {
        this.solde = solde;
    }

    public BigDecimal getTotalEncaisse() {
        return totalEncaisse;
    }

    public void setTotalEncaisse(BigDecimal totalEncaisse) {
        this.totalEncaisse = totalEncaisse;
    }

    public BigDecimal getTotalAchatsPieces() {
        return totalAchatsPieces;
    }

    public void setTotalAchatsPieces(BigDecimal totalAchatsPieces) {
        this.totalAchatsPieces = totalAchatsPieces;
    }

    public BigDecimal getTotalVerse() {
        return totalVerse;
    }

    public void setTotalVerse(BigDecimal totalVerse) {
        this.totalVerse = totalVerse;
    }

    public BigDecimal getCommissionAcquise() {
        return commissionAcquise;
    }

    public void setCommissionAcquise(BigDecimal commissionAcquise) {
        this.commissionAcquise = commissionAcquise;
    }

    public BigDecimal getCommissionPayee() {
        return commissionPayee;
    }

    public void setCommissionPayee(BigDecimal commissionPayee) {
        this.commissionPayee = commissionPayee;
    }

    /** Commission encore due au réparateur = acquise − payée (RG-38). */
    public BigDecimal getCommissionDue() {
        return commissionAcquise.subtract(commissionPayee);
    }

    public BigDecimal getCommissionPrevisionnelle() {
        return commissionPrevisionnelle;
    }

    public void setCommissionPrevisionnelle(BigDecimal commissionPrevisionnelle) {
        this.commissionPrevisionnelle = commissionPrevisionnelle;
    }
}
