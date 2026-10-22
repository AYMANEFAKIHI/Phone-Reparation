package ma.phonerepair.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Diagnostic, pièces et main d'œuvre d'un appareil. Le taux de commission est figé à la création (RG-33).
 */
public class Intervention {

    private int id;
    private int appareilId;
    private int reparateurId;
    private String diagnostic;
    private String piecesDescription;
    private BigDecimal coutAchatPieces;
    private BigDecimal prixPiecesClient;
    private BigDecimal mainOeuvre;
    private BigDecimal tauxCommissionApplique;
    private LocalDateTime dateIntervention;

    public Intervention() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAppareilId() {
        return appareilId;
    }

    public void setAppareilId(int appareilId) {
        this.appareilId = appareilId;
    }

    public int getReparateurId() {
        return reparateurId;
    }

    public void setReparateurId(int reparateurId) {
        this.reparateurId = reparateurId;
    }

    public String getDiagnostic() {
        return diagnostic;
    }

    public void setDiagnostic(String diagnostic) {
        this.diagnostic = diagnostic;
    }

    public String getPiecesDescription() {
        return piecesDescription;
    }

    public void setPiecesDescription(String piecesDescription) {
        this.piecesDescription = piecesDescription;
    }

    public BigDecimal getCoutAchatPieces() {
        return coutAchatPieces;
    }

    public void setCoutAchatPieces(BigDecimal coutAchatPieces) {
        this.coutAchatPieces = coutAchatPieces;
    }

    public BigDecimal getPrixPiecesClient() {
        return prixPiecesClient;
    }

    public void setPrixPiecesClient(BigDecimal prixPiecesClient) {
        this.prixPiecesClient = prixPiecesClient;
    }

    public BigDecimal getMainOeuvre() {
        return mainOeuvre;
    }

    public void setMainOeuvre(BigDecimal mainOeuvre) {
        this.mainOeuvre = mainOeuvre;
    }

    public BigDecimal getTauxCommissionApplique() {
        return tauxCommissionApplique;
    }

    public void setTauxCommissionApplique(BigDecimal tauxCommissionApplique) {
        this.tauxCommissionApplique = tauxCommissionApplique;
    }

    public LocalDateTime getDateIntervention() {
        return dateIntervention;
    }

    public void setDateIntervention(LocalDateTime dateIntervention) {
        this.dateIntervention = dateIntervention;
    }

    /** Montant facturé au client pour cette intervention = prix des pièces + main d'œuvre (RG-23). */
    public BigDecimal getMontantClient() {
        BigDecimal pieces = prixPiecesClient == null ? BigDecimal.ZERO : prixPiecesClient;
        BigDecimal mo = mainOeuvre == null ? BigDecimal.ZERO : mainOeuvre;
        return pieces.add(mo);
    }
}
