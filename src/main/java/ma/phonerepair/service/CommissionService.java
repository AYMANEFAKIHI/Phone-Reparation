package ma.phonerepair.service;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import ma.phonerepair.dao.InterventionDAO;
import ma.phonerepair.model.Intervention;
import ma.phonerepair.util.MoneyUtil;

/**
 * Calcul des commissions (RG-31 à RG-35). La commission porte uniquement sur la main d'œuvre.
 * Chaque intervention utilise SON taux figé (taux_commission_applique), jamais le taux actuel.
 */
public class CommissionService {

    private final InterventionDAO interventionDAO = new InterventionDAO();

    /** Commission du réparateur = main d'œuvre × taux / 100. Ex. 100 DH à 60 % → 60,00 DH. */
    public static BigDecimal calculerCommission(BigDecimal mainOeuvre, BigDecimal taux) {
        return MoneyUtil.pourcentage(mainOeuvre, taux);
    }

    /** Part du propriétaire sur la main d'œuvre = main d'œuvre − commission. Ex. 100 − 60 = 40,00 DH. */
    public static BigDecimal calculerPartProprietaire(BigDecimal mainOeuvre, BigDecimal taux) {
        return MoneyUtil.arrondir(mainOeuvre).subtract(calculerCommission(mainOeuvre, taux));
    }

    /** Marge sur les pièces = prix facturé − coût d'achat (peut être négative : geste commercial, RG-22). */
    public static BigDecimal calculerMargePieces(BigDecimal prixClient, BigDecimal coutAchat) {
        return MoneyUtil.arrondir(prixClient).subtract(MoneyUtil.arrondir(coutAchat));
    }

    /** Somme des commissions d'une liste d'interventions, chacune avec son propre taux figé. */
    public static BigDecimal totalCommissions(List<Intervention> interventions) {
        return interventions.stream()
                .map(i -> calculerCommission(i.getMainOeuvre(), i.getTauxCommissionApplique()))
                .reduce(MoneyUtil.ZERO, BigDecimal::add);
    }

    /** Commission acquise : appareils livrés (RG-35, Q2). */
    BigDecimal commissionAcquise(Connection conn, int reparateurId) throws SQLException {
        return totalCommissions(interventionDAO.listerPourCommission(conn, reparateurId, true));
    }

    /** Commission prévisionnelle : appareils chiffrés, ni livrés ni annulés. */
    BigDecimal commissionPrevisionnelle(Connection conn, int reparateurId) throws SQLException {
        return totalCommissions(interventionDAO.listerPourCommission(conn, reparateurId, false));
    }
}
