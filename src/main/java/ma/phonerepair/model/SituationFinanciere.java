package ma.phonerepair.model;

import java.math.BigDecimal;
import ma.phonerepair.model.enums.StatutPaiement;

/**
 * Situation de paiement d'une réparation à un instant donné (RG-27, RG-28).
 * Objet en lecture seule, calculé par PaiementService — jamais stocké en base.
 */
public class SituationFinanciere {

    private final BigDecimal total;
    private final BigDecimal paye;
    private final BigDecimal reste;
    private final StatutPaiement statut;

    public SituationFinanciere(BigDecimal total, BigDecimal paye, BigDecimal reste, StatutPaiement statut) {
        this.total = total;
        this.paye = paye;
        this.reste = reste;
        this.statut = statut;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public BigDecimal getPaye() {
        return paye;
    }

    public BigDecimal getReste() {
        return reste;
    }

    public StatutPaiement getStatut() {
        return statut;
    }
}
