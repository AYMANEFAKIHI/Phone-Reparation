package ma.phonerepair.presentation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

/** Petite carte « indicateur » : un libellé, une valeur en grand, une aide facultative. */
public class CarteChiffre extends JPanel {

    private final JLabel valeur = new JLabel("—");

    public CarteChiffre(String titre, String aide, Color couleur) {
        super(new BorderLayout(0, 2));
        setBackground(UiTheme.BLANC);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, couleur),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        JLabel t = new JLabel(titre);
        t.setForeground(UiTheme.TEXTE_SECONDAIRE);
        add(t, BorderLayout.NORTH);
        valeur.setFont(new Font(UiTheme.TITRE.getFamily(), Font.BOLD, 20));
        valeur.setForeground(UiTheme.TEXTE);
        add(valeur, BorderLayout.CENTER);
        if (aide != null) {
            add(UiTheme.aide(aide), BorderLayout.SOUTH);
        }
    }

    public void setValeur(String texte) {
        valeur.setText(texte);
    }
}
