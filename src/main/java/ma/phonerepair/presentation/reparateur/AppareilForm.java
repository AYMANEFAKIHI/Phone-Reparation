package ma.phonerepair.presentation.reparateur;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import ma.phonerepair.model.Appareil;
import ma.phonerepair.presentation.UiTheme;

/** Formulaire de saisie d'un appareil, réutilisé à la création et à l'ajout d'un appareil. */
public class AppareilForm extends JPanel {

    private final JTextField champMarque = new JTextField(14);
    private final JTextField champModele = new JTextField(14);
    private final JTextField champImei = new JTextField(14);
    private final JTextField champEtat = new JTextField(14);
    private final JTextField champDescription = new JTextField(14);
    private final JTextField champPanne = new JTextField(36);

    public AppareilForm() {
        super(new GridBagLayout());
        setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        cellule(c, 0, 0, "Marque *", champMarque);
        cellule(c, 2, 0, "Modèle *", champModele);
        cellule(c, 0, 1, "IMEI / N° série", champImei);
        cellule(c, 2, 1, "État physique", champEtat);
        cellule(c, 0, 2, "Description", champDescription);
        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 1;
        add(new JLabel("Panne déclarée *"), c);
        c.gridx = 1;
        c.gridwidth = 3;
        champPanne.setMinimumSize(champPanne.getPreferredSize());
        add(champPanne, c);
        c.gridx = 1;
        c.gridy = 4;
        add(UiTheme.aide("Ex. : Apple / iPhone 13 / « Écran cassé, tactile ne répond plus »"), c);
    }

    private void cellule(GridBagConstraints c, int x, int y, String libelle, JComponent champ) {
        c.gridwidth = 1;
        c.gridx = x;
        c.gridy = y;
        add(new JLabel(libelle), c);
        c.gridx = x + 1;
        c.weightx = 1;
        champ.setMinimumSize(champ.getPreferredSize());   // évite l'écrasement des champs par GridBagLayout
        add(champ, c);
        c.weightx = 0;
    }

    /** Lit la saisie. La validation est faite par le service. */
    public Appareil lire() {
        Appareil a = new Appareil();
        a.setMarque(champMarque.getText());
        a.setModele(champModele.getText());
        a.setImeiSerie(champImei.getText());
        a.setEtatPhysique(champEtat.getText());
        a.setDescription(champDescription.getText());
        a.setProblemeSignale(champPanne.getText());
        return a;
    }

    public void vider() {
        for (JTextField f : new JTextField[] {champMarque, champModele, champImei, champEtat, champDescription,
                champPanne}) {
            f.setText("");
        }
        champMarque.requestFocusInWindow();
    }
}
