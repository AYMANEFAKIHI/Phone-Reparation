package ma.phonerepair.model;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * État de la caisse d'un réparateur (business-rules § 9). Tout est calculé, rien n'est stocké.
 * <ul>
 *   <li>solde = entrées − sorties (argent physiquement détenu)</li>
 *   <li>commission acquise = commissions des appareils livrés</li>
 *   <li>commission due = acquise − déjà payée</li>
 *   <li>commission prévisionnelle = appareils chiffrés pas encore livrés</li>
 * </ul>
 */
@Getter
@Setter
@NoArgsConstructor
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

    /** Commission encore due au réparateur = acquise − payée (RG-38). */
    public BigDecimal getCommissionDue() {
        return commissionAcquise.subtract(commissionPayee);
    }
}
