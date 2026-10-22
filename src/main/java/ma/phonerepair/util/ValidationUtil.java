package ma.phonerepair.util;

import java.util.regex.Pattern;

/**
 * Vérifications de format réutilisables. Ne lève pas d'exception :
 * c'est le service qui décide du message d'erreur.
 */
public final class ValidationUtil {

    /** Téléphone marocain : 05/06/07 + 8 chiffres, ou +212 + 9 chiffres (RG-07). */
    private static final Pattern TELEPHONE_MAROC = Pattern.compile("^(0[5-7]\\d{8}|\\+212[5-7]\\d{8})$");
    private static final Pattern USERNAME = Pattern.compile("^[a-zA-Z0-9._-]{3,50}$");
    public static final int MOT_DE_PASSE_MIN = 6;

    private ValidationUtil() {
    }

    public static boolean estVide(String valeur) {
        return valeur == null || valeur.isBlank();
    }

    /** Retire espaces, points et tirets : "06 12-34.56.78" → "0612345678". */
    public static String normaliserTelephone(String telephone) {
        return telephone == null ? null : telephone.replaceAll("[\\s.\\-]", "");
    }

    public static boolean estTelephoneValide(String telephone) {
        return telephone != null && TELEPHONE_MAROC.matcher(normaliserTelephone(telephone)).matches();
    }

    public static boolean estUsernameValide(String username) {
        return username != null && USERNAME.matcher(username).matches();
    }

    public static boolean estMotDePasseValide(char[] motDePasse) {
        return motDePasse != null && motDePasse.length >= MOT_DE_PASSE_MIN;
    }

    public static boolean depasse(String valeur, int longueurMax) {
        return valeur != null && valeur.length() > longueurMax;
    }
}
