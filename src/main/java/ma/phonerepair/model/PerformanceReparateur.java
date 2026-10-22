package ma.phonerepair.model;

import java.math.BigDecimal;

/** Chiffres d'un réparateur sur une période (EF-22) : basés sur les appareils livrés. */
public class PerformanceReparateur {

    private final int reparateurId;
    private final String nomReparateur;
    private int appareilsLivres;
    private BigDecimal montantFacture = BigDecimal.ZERO.setScale(2);
    private BigDecimal mainOeuvre = BigDecimal.ZERO.setScale(2);
    private BigDecimal commission = BigDecimal.ZERO.setScale(2);

    public PerformanceReparateur(int reparateurId, String nomReparateur) {
        this.reparateurId = reparateurId;
        this.nomReparateur = nomReparateur;
    }

    public int getReparateurId() {
        return reparateurId;
    }

    public String getNomReparateur() {
        return nomReparateur;
    }

    public int getAppareilsLivres() {
        return appareilsLivres;
    }

    public void setAppareilsLivres(int appareilsLivres) {
        this.appareilsLivres = appareilsLivres;
    }

    public BigDecimal getMontantFacture() {
        return montantFacture;
    }

    public void setMontantFacture(BigDecimal montantFacture) {
        this.montantFacture = montantFacture;
    }

    public BigDecimal getMainOeuvre() {
        return mainOeuvre;
    }

    public void setMainOeuvre(BigDecimal mainOeuvre) {
        this.mainOeuvre = mainOeuvre;
    }

    public BigDecimal getCommission() {
        return commission;
    }

    public void setCommission(BigDecimal commission) {
        this.commission = commission;
    }
}
