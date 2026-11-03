package ma.phonerepair.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Diagnostic, pièces et main d'œuvre d'un appareil. Le taux de commission est figé à la création (RG-33).
 */
@Getter
@Setter
@NoArgsConstructor
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

    /** Montant facturé au client pour cette intervention = prix des pièces + main d'œuvre (RG-23). */
    public BigDecimal getMontantClient() {
        BigDecimal pieces = prixPiecesClient == null ? BigDecimal.ZERO : prixPiecesClient;
        BigDecimal mo = mainOeuvre == null ? BigDecimal.ZERO : mainOeuvre;
        return pieces.add(mo);
    }
}
