package ma.phonerepair.presentation;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.JTableHeader;

/**
 * Identité visuelle de l'application : couleurs, polices et composants stylés.
 * Tous les écrans passent par cette classe pour rester cohérents.
 */
public final class UiTheme {

    public static final Color PRIMAIRE = new Color(0x1E3A5F);       // bleu nuit
    public static final Color PRIMAIRE_CLAIR = new Color(0x2C5282);
    public static final Color ACCENT = new Color(0xE07A1F);         // orange
    public static final Color FOND = new Color(0xF4F6F9);
    public static final Color BLANC = Color.WHITE;
    public static final Color TEXTE = new Color(0x1F2933);
    public static final Color TEXTE_SECONDAIRE = new Color(0x616E7C);
    public static final Color BORDURE = new Color(0xD9DEE5);
    public static final Color SUCCES = new Color(0x2F855A);
    public static final Color DANGER = new Color(0xC53030);
    public static final Color GRIS = new Color(0x718096);

    private static final String FAMILLE = choisirPolice();
    public static final Font POLICE = new Font(FAMILLE, Font.PLAIN, 14);
    public static final Font POLICE_GRAS = new Font(FAMILLE, Font.BOLD, 14);
    public static final Font TITRE = new Font(FAMILLE, Font.BOLD, 22);
    public static final Font SOUS_TITRE = new Font(FAMILLE, Font.BOLD, 16);

    private UiTheme() {
    }

    private static String choisirPolice() {
        String[] disponibles = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getAvailableFontFamilyNames();
        for (String souhaitee : new String[] {"Segoe UI", "Ubuntu", "DejaVu Sans"}) {
            for (String d : disponibles) {
                if (d.equals(souhaitee)) {
                    return d;
                }
            }
        }
        return Font.SANS_SERIF;
    }

    /** À appeler une fois au démarrage, avant de créer la première fenêtre. */
    public static void installer() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // On garde le look & feel par défaut : pas bloquant.
        }
        for (String cle : new String[] {"Label.font", "TextField.font", "PasswordField.font", "ComboBox.font",
                "Table.font", "TableHeader.font", "OptionPane.messageFont", "OptionPane.buttonFont",
                "Spinner.font", "TextArea.font", "CheckBox.font", "Button.font"}) {
            UIManager.put(cle, POLICE);
        }
    }

    public static JLabel titre(String texte) {
        JLabel l = new JLabel(texte);
        l.setFont(TITRE);
        l.setForeground(PRIMAIRE);
        return l;
    }

    public static JLabel sousTitre(String texte) {
        JLabel l = new JLabel(texte);
        l.setFont(SOUS_TITRE);
        l.setForeground(TEXTE);
        return l;
    }

    public static JLabel aide(String texte) {
        JLabel l = new JLabel(texte);
        l.setFont(POLICE.deriveFont(12f));
        l.setForeground(TEXTE_SECONDAIRE);
        return l;
    }

    public static JButton boutonPrincipal(String texte) {
        return new BoutonPlat(texte, PRIMAIRE, BLANC);
    }

    public static JButton boutonAccent(String texte) {
        return new BoutonPlat(texte, ACCENT, BLANC);
    }

    public static JButton boutonSecondaire(String texte) {
        return new BoutonPlat(texte, new Color(0xE2E8F0), TEXTE);
    }

    public static JButton boutonDanger(String texte) {
        return new BoutonPlat(texte, DANGER, BLANC);
    }

    /** Bouton du menu latéral. */
    public static JButton boutonMenu(String texte) {
        BoutonPlat b = new BoutonPlat(texte, PRIMAIRE, BLANC);
        b.setHorizontalAlignment(JButton.LEFT);
        b.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 10));
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        b.arrondi = 0;
        return b;
    }

    /** Couleur associée à un statut (appareil ou réparation), pour les tableaux. */
    public static Color couleurStatut(String codeStatut) {
        return switch (codeStatut) {
            case "READY" -> SUCCES;
            case "DELIVERED" -> GRIS;
            case "CANCELLED" -> DANGER;
            case "WAITING_FOR_PART", "WAITING_FOR_APPROVAL" -> ACCENT;
            case "RECEIVED" -> PRIMAIRE_CLAIR;
            default -> PRIMAIRE;
        };
    }

    /** Rendu « pastille colorée » pour une colonne contenant un enum de statut. */
    public static javax.swing.table.TableCellRenderer rendreStatut() {
        return new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(JTable t, Object valeur, boolean sel,
                                                                    boolean focus, int l, int c) {
                super.getTableCellRendererComponent(t, valeur, sel, focus, l, c);
                if (valeur instanceof Enum<?> e) {
                    setForeground(couleurStatut(e.name()));
                    setFont(POLICE_GRAS);
                }
                return this;
            }
        };
    }

    /** Aligne à droite une colonne de montants. */
    public static javax.swing.table.TableCellRenderer rendreMontant() {
        javax.swing.table.DefaultTableCellRenderer r = new javax.swing.table.DefaultTableCellRenderer();
        r.setHorizontalAlignment(JLabel.RIGHT);
        return r;
    }

    public static void styliserTable(JTable table) {
        table.setRowHeight(28);
        table.setFont(POLICE);
        table.setGridColor(BORDURE);
        table.setShowVerticalLines(false);
        table.setSelectionBackground(new Color(0xBEE3F8));
        table.setSelectionForeground(TEXTE);
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(true);
        JTableHeader entete = table.getTableHeader();
        entete.setFont(POLICE_GRAS);
        entete.setReorderingAllowed(false);
    }

    /**
     * Bouton dessiné à la main : rend la même couleur quel que soit le look & feel
     * du système (le L&F Windows ignore setBackground sur les JButton).
     */
    public static class BoutonPlat extends JButton {
        private final Color fond;
        private int arrondi = 8;
        private boolean actif;

        public BoutonPlat(String texte, Color fond, Color couleurTexte) {
            super(texte);
            this.fond = fond;
            setForeground(couleurTexte);
            setFont(POLICE_GRAS);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        /** Marque l'entrée de menu courante. */
        public void setActif(boolean actif) {
            this.actif = actif;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color c = fond;
            if (!isEnabled()) {
                c = new Color(0xCBD5E0);
            } else if (actif) {
                c = ACCENT;
            } else if (getModel().isPressed()) {
                c = fond.darker();
            } else if (getModel().isRollover()) {
                c = fond == PRIMAIRE ? PRIMAIRE_CLAIR : fond.brighter();
            }
            g2.setColor(c);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), arrondi, arrondi);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
