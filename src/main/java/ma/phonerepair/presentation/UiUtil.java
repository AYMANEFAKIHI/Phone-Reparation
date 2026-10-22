package ma.phonerepair.presentation;

import java.awt.Component;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import ma.phonerepair.dao.DataAccessException;
import ma.phonerepair.service.exception.AccesRefuseException;
import ma.phonerepair.service.exception.BusinessException;

/**
 * Point unique d'affichage des messages et des erreurs.
 * Les écrans appellent un service dans {@link #executer} : aucune exception
 * technique (SQL...) n'est jamais montrée telle quelle à l'utilisateur.
 */
public final class UiUtil {

    private static final Logger LOG = Logger.getLogger(UiUtil.class.getName());

    /** Action d'écran qui appelle un service. */
    @FunctionalInterface
    public interface Action {
        void executer();
    }

    private UiUtil() {
    }

    /** @return true si l'action s'est terminée sans erreur */
    public static boolean executer(Component parent, Action action) {
        try {
            action.executer();
            return true;
        } catch (AccesRefuseException e) {
            JOptionPane.showMessageDialog(parent, e.getMessage(), "Accès refusé", JOptionPane.WARNING_MESSAGE);
        } catch (BusinessException e) {
            // Inclut ValidationException : le message est déjà rédigé pour l'utilisateur.
            JOptionPane.showMessageDialog(parent, e.getMessage(), "Attention", JOptionPane.WARNING_MESSAGE);
        } catch (DataAccessException e) {
            JOptionPane.showMessageDialog(parent,
                    "Erreur technique lors de l'accès à la base de données.\n"
                    + "Vérifiez que MySQL est démarré puis réessayez.",
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        } catch (RuntimeException e) {
            LOG.log(Level.SEVERE, "Erreur inattendue", e);
            JOptionPane.showMessageDialog(parent,
                    "Une erreur inattendue s'est produite. Détails dans logs/repairtrack.log.",
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
        return false;
    }

    /**
     * Comme {@link #executer} mais pour un appel qui renvoie une valeur.
     *
     * @return la valeur, ou null si une erreur a été affichée
     */
    public static <T> T appeler(Component parent, Supplier<T> appel) {
        Object[] resultat = new Object[1];
        boolean ok = executer(parent, () -> resultat[0] = appel.get());
        @SuppressWarnings("unchecked")
        T valeur = ok ? (T) resultat[0] : null;
        return valeur;
    }

    public static void succes(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Succès", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void avertir(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Attention", JOptionPane.WARNING_MESSAGE);
    }

    public static boolean confirmer(Component parent, String message) {
        return JOptionPane.showConfirmDialog(parent, message, "Confirmation",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }
}
