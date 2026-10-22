package ma.phonerepair.util;

/**
 * Format du code de suivi d'une réparation : REP-AAAA-NNNNNN (RG-11).
 * Le calcul du prochain numéro (requête SQL) est fait par ReparationDAO.
 */
public final class CodeGenerator {

    public static final String PREFIXE = "REP";
    private static final int CHIFFRES = 6;
    private static final int MAX = 999_999;

    private CodeGenerator() {
    }

    /** formater(2026, 123) → "REP-2026-000123" */
    public static String formater(int annee, int numero) {
        if (numero < 1 || numero > MAX) {
            throw new IllegalArgumentException("Numéro de réparation hors limites : " + numero);
        }
        return String.format("%s-%04d-%0" + CHIFFRES + "d", PREFIXE, annee, numero);
    }

    /** Préfixe utilisé pour chercher les codes d'une année : "REP-2026-" */
    public static String prefixeAnnee(int annee) {
        return String.format("%s-%04d-", PREFIXE, annee);
    }

    /** numero("REP-2026-000123") → 123 */
    public static int numero(String code) {
        if (!estFormatValide(code)) {
            throw new IllegalArgumentException("Code de suivi invalide : " + code);
        }
        return Integer.parseInt(code.substring(code.length() - CHIFFRES));
    }

    public static boolean estFormatValide(String code) {
        return code != null && code.matches("^REP-\\d{4}-\\d{6}$");
    }

    /** Normalise une saisie client : " rep-2026-000123 " → "REP-2026-000123". */
    public static String normaliser(String saisie) {
        return saisie == null ? null : saisie.trim().toUpperCase();
    }
}
