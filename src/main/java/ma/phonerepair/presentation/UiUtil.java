package ma.phonerepair.presentation;

import java.awt.Component;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import ma.phonerepair.dao.DataAccessException;
import ma.phonerepair.service.exception.AccesRefuseException;
import ma.phonerepair.service.exception.BusinessException;

/**
 * Point unique d'affichage des messages et des erreurs, + raccourcis pour brancher
 * des lambdas sur les événements Swing.
 * <p>
 * Les écrans appellent un service dans {@link #executer} : aucune exception
 * technique (SQL...) n'est jamais montrée telle quelle à l'utilisateur.
 */
public final class UiUtil {

    private static final Logger LOG = Logger.getLogger(UiUtil.class.getName());

    private UiUtil() {
    }

    // ------------------------------------------------------------------ appels de service

    /**
     * Appelle un service qui renvoie une valeur.
     *
     * @return la valeur, ou null si une erreur a été affichée
     */
    public static <T> T appeler(Component parent, Supplier<T> appel) {
        try {
            return appel.get();
        } catch (RuntimeException e) {
            afficherErreur(parent, e);
            return null;
        }
    }

    /** @return true si l'action s'est terminée sans erreur */
    public static boolean executer(Component parent, Runnable action) {
        return appeler(parent, () -> {
            action.run();
            return Boolean.TRUE;
        }) != null;
    }

    private static void afficherErreur(Component parent, RuntimeException e) {
        if (e instanceof AccesRefuseException) {
            JOptionPane.showMessageDialog(parent, e.getMessage(), "Accès refusé", JOptionPane.WARNING_MESSAGE);
        } else if (e instanceof BusinessException) {
            // Inclut ValidationException : le message est déjà rédigé pour l'utilisateur.
            avertir(parent, e.getMessage());
        } else if (e instanceof DataAccessException) {
            JOptionPane.showMessageDialog(parent, "Erreur technique lors de l'accès à la base de données.\n"
                    + "Vérifiez que MySQL est démarré puis réessayez.", "Erreur", JOptionPane.ERROR_MESSAGE);
        } else {
            LOG.log(Level.SEVERE, "Erreur inattendue", e);
            JOptionPane.showMessageDialog(parent, "Une erreur inattendue s'est produite. "
                    + "Détails dans logs/repairtrack.log.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ------------------------------------------------------------------ messages

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

    // ------------------------------------------------------------------ événements → lambdas

    /** Lance {@code action} chaque fois que l'écran redevient visible. Ex. {@code quandAffiche(this, this::charger)} */
    public static void quandAffiche(Component ecran, Runnable action) {
        ecran.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                action.run();
            }
        });
    }

    /** Lance {@code action} sur un double-clic (ex. une ligne de tableau). */
    public static void surDoubleClic(Component composant, Runnable action) {
        composant.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    action.run();
                }
            }
        });
    }

    /** Lance {@code action} à chaque frappe dans l'un des champs. */
    public static void surSaisie(Runnable action, JTextComponent... champs) {
        DocumentListener listener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                action.run();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                action.run();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                action.run();
            }
        };
        for (JTextComponent champ : champs) {
            champ.getDocument().addDocumentListener(listener);
        }
    }
}
