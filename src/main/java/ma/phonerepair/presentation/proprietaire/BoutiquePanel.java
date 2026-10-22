package ma.phonerepair.presentation.proprietaire;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import ma.phonerepair.model.Boutique;
import ma.phonerepair.model.Utilisateur;
import ma.phonerepair.presentation.UiTheme;
import ma.phonerepair.presentation.UiUtil;
import ma.phonerepair.service.BoutiqueService;

/** Configuration de la boutique par le propriétaire (EF-03, scénario S1). */
public class BoutiquePanel extends JPanel {

    private final BoutiqueService boutiqueService = new BoutiqueService();
    private final Utilisateur acteur;
    private final Runnable apresEnregistrement;

    private final JTextField champNom = new JTextField(28);
    private final JTextField champAdresse = new JTextField(28);
    private final JTextField champTelephone = new JTextField(28);
    private final JTextField champPatente = new JTextField(28);

    public BoutiquePanel(Utilisateur acteur, Runnable apresEnregistrement) {
        super(new BorderLayout());
        this.acteur = acteur;
        this.apresEnregistrement = apresEnregistrement;
        setBackground(UiTheme.FOND);
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        JPanel entete = new JPanel(new BorderLayout());
        entete.setOpaque(false);
        entete.add(UiTheme.titre("Ma boutique"), BorderLayout.NORTH);
        entete.add(UiTheme.aide("Ces informations identifient votre boutique (N° de patente obligatoire)."),
                BorderLayout.SOUTH);
        add(entete, BorderLayout.NORTH);
        add(construireFormulaire(), BorderLayout.CENTER);
        charger();
    }

    private JPanel construireFormulaire() {
        JPanel carte = new JPanel(new GridBagLayout());
        carte.setBackground(UiTheme.BLANC);
        carte.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiTheme.BORDURE),
                BorderFactory.createEmptyBorder(20, 24, 20, 24)));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.anchor = GridBagConstraints.WEST;
        ligne(carte, c, 0, "Nom de la boutique *", champNom);
        ligne(carte, c, 1, "Adresse *", champAdresse);
        ligne(carte, c, 2, "Téléphone *", champTelephone);
        ligne(carte, c, 3, "N° de patente *", champPatente);

        JButton enregistrer = UiTheme.boutonPrincipal("Enregistrer");
        enregistrer.addActionListener(e -> enregistrer());
        c.gridx = 1;
        c.gridy = 4;
        c.insets = new Insets(16, 6, 6, 6);
        carte.add(enregistrer, c);

        JPanel conteneur = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 16));
        conteneur.setOpaque(false);
        conteneur.add(carte);
        return conteneur;
    }

    private static void ligne(JPanel p, GridBagConstraints c, int y, String libelle, JTextField champ) {
        c.gridx = 0;
        c.gridy = y;
        p.add(new JLabel(libelle), c);
        c.gridx = 1;
        p.add(champ, c);
    }

    private void charger() {
        UiUtil.executer(this, () -> boutiqueService.obtenir().ifPresent(b -> {
            champNom.setText(b.getNom());
            champAdresse.setText(b.getAdresse());
            champTelephone.setText(b.getTelephone());
            champPatente.setText(b.getNumeroPatente());
        }));
    }

    private void enregistrer() {
        Boutique b = new Boutique();
        b.setNom(champNom.getText());
        b.setAdresse(champAdresse.getText());
        b.setTelephone(champTelephone.getText());
        b.setNumeroPatente(champPatente.getText());
        if (UiUtil.executer(this, () -> boutiqueService.enregistrer(acteur, b))) {
            charger();
            apresEnregistrement.run();
            UiUtil.succes(this, "Informations de la boutique enregistrées.");
        }
    }
}
