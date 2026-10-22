package ma.phonerepair.util;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Hachage des mots de passe avec PBKDF2-HMAC-SHA256 (inclus dans le JDK).
 * <p>
 * Format stocké : {@code pbkdf2$<iterations>$<sel base64>$<hash base64>}.
 * Le sel est aléatoire : deux utilisateurs avec le même mot de passe ont des hash différents.
 */
public final class PasswordUtil {

    private static final String ALGORITHME = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 120_000;
    private static final int TAILLE_SEL = 16;
    private static final int TAILLE_HASH_BITS = 256;
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordUtil() {
    }

    public static String hacher(char[] motDePasse) {
        byte[] sel = new byte[TAILLE_SEL];
        RANDOM.nextBytes(sel);
        byte[] hash = pbkdf2(motDePasse, sel, ITERATIONS);
        Base64.Encoder b64 = Base64.getEncoder();
        return "pbkdf2$" + ITERATIONS + "$" + b64.encodeToString(sel) + "$" + b64.encodeToString(hash);
    }

    public static boolean verifier(char[] motDePasse, String hashStocke) {
        if (motDePasse == null || hashStocke == null) {
            return false;
        }
        String[] parties = hashStocke.split("\\$");
        if (parties.length != 4 || !"pbkdf2".equals(parties[0])) {
            return false;
        }
        try {
            int iterations = Integer.parseInt(parties[1]);
            byte[] sel = Base64.getDecoder().decode(parties[2]);
            byte[] attendu = Base64.getDecoder().decode(parties[3]);
            byte[] calcule = pbkdf2(motDePasse, sel, iterations);
            // Comparaison en temps constant
            return MessageDigest.isEqual(attendu, calcule);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static byte[] pbkdf2(char[] motDePasse, byte[] sel, int iterations) {
        PBEKeySpec spec = new PBEKeySpec(motDePasse, sel, iterations, TAILLE_HASH_BITS);
        try {
            return SecretKeyFactory.getInstance(ALGORITHME).generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Algorithme de hachage indisponible : " + ALGORITHME, e);
        } finally {
            spec.clearPassword();
        }
    }

    /** Efface un mot de passe de la mémoire après usage. */
    public static void effacer(char[] motDePasse) {
        if (motDePasse != null) {
            Arrays.fill(motDePasse, '\0');
        }
    }

    /** Petit outil : génère un hash pour data.sql. Usage : java PasswordUtil monMotDePasse */
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage : PasswordUtil <motDePasse>");
            return;
        }
        System.out.println(hacher(args[0].toCharArray()));
    }
}
