package ma.phonerepair.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PasswordUtilTest {

    @Test
    void leBonMotDePasseEstAccepte() {
        String hash = PasswordUtil.hacher("secret123".toCharArray());
        assertTrue(PasswordUtil.verifier("secret123".toCharArray(), hash));
    }

    @Test
    void unMauvaisMotDePasseEstRefuse() {
        String hash = PasswordUtil.hacher("secret123".toCharArray());
        assertFalse(PasswordUtil.verifier("secret124".toCharArray(), hash));
    }

    @Test
    void leMotDePasseNestJamaisStockeEnClair() {
        String hash = PasswordUtil.hacher("secret123".toCharArray());
        assertFalse(hash.contains("secret123"));
        assertTrue(hash.startsWith("pbkdf2$"));
    }

    @Test
    void deuxHashDuMemeMotDePasseSontDifferents() {
        // sel aléatoire
        assertNotEquals(PasswordUtil.hacher("abc123".toCharArray()), PasswordUtil.hacher("abc123".toCharArray()));
    }

    @Test
    void unHashMalFormeEstRefuseSansException() {
        assertFalse(PasswordUtil.verifier("x".toCharArray(), "n'importe quoi"));
        assertFalse(PasswordUtil.verifier("x".toCharArray(), null));
    }

    @Test
    void leHashDuCompteOwnerDeDataSqlEstValide() {
        String hashDataSql = "pbkdf2$120000$tYwTiyzrJRZU/LlJVs7a6g==$NUoBC68jlox+ycbbfjSVVlEhCZQKsHCdoAf/KQXnW7k=";
        assertTrue(PasswordUtil.verifier("owner123".toCharArray(), hashDataSql));
    }
}
