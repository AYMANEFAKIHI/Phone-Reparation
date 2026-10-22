package ma.phonerepair.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Opérations sur les montants (toujours en BigDecimal, 2 décimales, arrondi HALF_UP — RG-21).
 */
public final class MoneyUtil {

    public static final int ECHELLE = 2;
    public static final RoundingMode ARRONDI = RoundingMode.HALF_UP;
    public static final BigDecimal ZERO = BigDecimal.ZERO.setScale(ECHELLE);
    private static final BigDecimal CENT = new BigDecimal("100");

    private MoneyUtil() {
    }

    public static BigDecimal arrondir(BigDecimal montant) {
        return montant == null ? ZERO : montant.setScale(ECHELLE, ARRONDI);
    }

    /** montant × taux / 100, arrondi à 2 décimales. Ex. : pourcentage(100, 60) = 60.00 */
    public static BigDecimal pourcentage(BigDecimal montant, BigDecimal taux) {
        if (montant == null || taux == null) {
            return ZERO;
        }
        return montant.multiply(taux).divide(CENT, ECHELLE, ARRONDI);
    }

    public static boolean estNegatif(BigDecimal montant) {
        return montant != null && montant.signum() < 0;
    }

    public static boolean estStrictementPositif(BigDecimal montant) {
        return montant != null && montant.signum() > 0;
    }

    /**
     * Convertit une saisie utilisateur ("1 250,50", "1250.5", "300") en BigDecimal.
     *
     * @throws NumberFormatException si la saisie n'est pas un nombre
     */
    public static BigDecimal parser(String saisie) {
        if (saisie == null || saisie.isBlank()) {
            throw new NumberFormatException("Montant vide");
        }
        String nettoye = saisie.trim()
                .replace("DH", "").replace("dh", "")
                .replace(" ", "").replace(" ", "").replace(" ", "")
                .replace(',', '.');
        BigDecimal valeur = new BigDecimal(nettoye);
        if (valeur.scale() > ECHELLE) {
            throw new NumberFormatException("Maximum 2 décimales");
        }
        return valeur.setScale(ECHELLE, ARRONDI);
    }

    /** Format d'affichage : "1 250,50 DH". */
    public static String formater(BigDecimal montant) {
        DecimalFormatSymbols symboles = new DecimalFormatSymbols(Locale.FRANCE);
        symboles.setGroupingSeparator(' ');
        DecimalFormat format = new DecimalFormat("#,##0.00", symboles);
        return format.format(arrondir(montant)) + " DH";
    }
}
