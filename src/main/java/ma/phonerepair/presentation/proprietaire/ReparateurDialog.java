package ma.phonerepair.presentation.proprietaire;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.math.BigDecimal;
import java.util.Arrays;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import ma.phonerepair.model.Utilisateur;
import ma.phonerepair.presentation.UiTheme;
import ma.phonerepair.presentation.UiUtil;
import ma.phonerepair.service.ReparateurService;
import ma.phonerepair.util.MoneyUtil;

/** Création d'un compte réparateur (EF-04, scénario S2). */
public class ReparateurDialog extends JDialog {

    private final ReparateurService reparateurService = new ReparateurService();
    private final Utilisateur acteur;

    private final JTextField champNom = new JTextField(22);
    private final JTextField champTelephone = new JTextField(22);
    private final JTextField champUsername = new JTextField(22);
    private final JPasswordField champMotDePasse = new JPasswordField(22);
    private final JPasswordField champConfirmation = new JPasswordField(22);
    private final JTextField champTaux = new JTextField(8);

    private boolean cree;

    public ReparateurDialog(Window parent, Utilisateur acteur) {
        super(parent, "Nouveau réparateur", ModalityType.APPLICATION_MODAL);
        this.acteur = acteur;

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(16, 20, 8, 20));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        c.anchor = GridBagConstraints.WEST;
        int y = 0;
        ligne(form, c, y++, "Nom complet *", champNom);
        ligne(form, c, y++, "Téléphone", champTelephone);
        ligne(form, c, y++, "Nom d'utilisateur *", champUsername);
        ligne(form, c, y++, "Mot de passe * (6 min.)", champMotDePasse);
        ligne(form, c, y++, "Confirmation *", champConfirmation);
        ligne(form, c, y++, "Commission (%) *", champTaux);
        c.gridx = 1;
        c.gridy = y;
        form.add(UiTheme.aide("Part du réparateur sur la main d'œuvre (ex. 60)."), c);

        JButton annuler = UiTheme.boutonSecondaire("Annuler");
        annuler.addActionListener(e -> dispose());
        JButton creer = UiTheme.boutonPrincipal("Créer le compte");
        creer.addActionListener(e -> creer());
        JPanel boutons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        boutons.setBorder(BorderFactory.createEmptyBorder(0, 16, 12, 16));
        boutons.add(annuler);
        boutons.add(creer);

        getContentPane().add(form, BorderLayout.CENTER);
        getContentPane().add(boutons, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(creer);
        pack();
        setResizable(false);
        setLocationRelativeTo(parent);
    }

    private static void ligne(JPanel p, GridBagConstraints c, int y, String libelle, JComponent champ) {
        c.gridx = 0;
        c.gridy = y;
        p.add(new JLabel(libelle), c);
        c.gridx = 1;
        p.add(champ, c);
    }

    private void creer() {
        char[] motDePasse = champMotDePasse.getPassword();
        if (!Arrays.equals(motDePasse, champConfirmation.getPassword())) {
            UiUtil.avertir(this, "La confirmation ne correspond pas au mot de passe.");
            return;
        }
        BigDecimal taux;
        try {
            taux = MoneyUtil.parser(champTaux.getText().replace("%", ""));
        } catch (NumberFormatException e) {
            UiUtil.avertir(this, "Le taux de commission doit être un nombre (ex. 60).");
            return;
        }
        if (UiUtil.executer(this, () -> reparateurService.creerReparateur(acteur, champNom.getText(),
                champTelephone.getText(), champUsername.getText(), motDePasse, taux))) {
            cree = true;
            UiUtil.succes(this, "Compte « " + champUsername.getText().trim() + " » créé.");
            dispose();
        }
    }

    /** @return true si un compte a été créé */
    public boolean afficher() {
        setVisible(true);
        return cree;
    }
}
