package ma.phonerepair.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ValidationUtilTest {

    @Test
    void telephonesMarocainsValides() {
        assertTrue(ValidationUtil.estTelephoneValide("0612345678"));
        assertTrue(ValidationUtil.estTelephoneValide("0712345678"));
        assertTrue(ValidationUtil.estTelephoneValide("0522123456"));
        assertTrue(ValidationUtil.estTelephoneValide("06 12 34 56 78"));
        assertTrue(ValidationUtil.estTelephoneValide("+212612345678"));
    }

    @Test
    void telephonesInvalides() {
        assertFalse(ValidationUtil.estTelephoneValide("061234567"));    // 9 chiffres
        assertFalse(ValidationUtil.estTelephoneValide("0812345678"));   // préfixe 08
        assertFalse(ValidationUtil.estTelephoneValide("06XXXXXXXX"));
        assertFalse(ValidationUtil.estTelephoneValide(null));
    }

    @Test
    void normalisationDuTelephone() {
        assertEquals("0612345678", ValidationUtil.normaliserTelephone("06 12-34.56 78"));
    }

    @Test
    void username() {
        assertTrue(ValidationUtil.estUsernameValide("ahmed"));
        assertTrue(ValidationUtil.estUsernameValide("a.el-amrani_2"));
        assertFalse(ValidationUtil.estUsernameValide("ab"));
        assertFalse(ValidationUtil.estUsernameValide("ahmed amrani"));
    }

    @Test
    void motDePasseMinimum6() {
        assertFalse(ValidationUtil.estMotDePasseValide("12345".toCharArray()));
        assertTrue(ValidationUtil.estMotDePasseValide("123456".toCharArray()));
    }
}
