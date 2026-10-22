package ma.phonerepair.presentation;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import ma.phonerepair.model.Boutique;
import ma.phonerepair.model.Utilisateur;
import ma.phonerepair.service.AuthService;
import ma.phonerepair.service.BoutiqueService;

/** Écran de connexion (EF-01). */
public class LoginFrame extends JFrame {

    private final AuthService authService = new AuthService();
    private final BoutiqueService boutiqueService = new BoutiqueService();

    private final JTextField champUsername = new JTextField(18);
    private final JPasswordField champMotDePasse = new JPasswordField(18);

    public LoginFrame() {
        super("RepairTrack — Connexion");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setContentPane(construire());
        setMinimumSize(new Dimension(460, 420));
        pack();
        setLocationRelativeTo(null);
    }

    private JPanel construire() {
        JPanel fond = new JPanel(new GridBagLayout());
        fond.setBackground(UiTheme.PRIMAIRE);

        JPanel carte = new JPanel(new GridBagLayout());
        carte.setBackground(UiTheme.BLANC);
        carte.setBorder(BorderFactory.createEmptyBorder(28, 36, 28, 36));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(4, 0, 4, 0);

        JLabel titre = UiTheme.titre("RepairTrack");
        titre.setHorizontalAlignment(SwingConstants.CENTER);
        carte.add(titre, c);

        JLabel sousTitre = UiTheme.aide(nomBoutique());
        sousTitre.setHorizontalAlignment(SwingConstants.CENTER);
        c.insets = new Insets(0, 0, 18, 0);
        carte.add(sousTitre, c);

        c.insets = new Insets(4, 0, 2, 0);
        carte.add(new JLabel("Nom d'utilisateur"), c);
        carte.add(champUsername, c);
        carte.add(new JLabel("Mot de passe"), c);
        carte.add(champMotDePasse, c);

        JButton connecter = UiTheme.boutonPrincipal("Se connecter");
        connecter.addActionListener(e -> seConnecter());
        c.insets = new Insets(18, 0, 4, 0);
        carte.add(connecter, c);
        getRootPane().setDefaultButton(connecter);   // touche Entrée

        JButton suivi = UiTheme.boutonSecondaire("Client : suivre ma réparation");
        suivi.addActionListener(e -> ouvrirSuivi());
        c.insets = new Insets(6, 0, 0, 0);
        carte.add(suivi, c);

        fond.add(carte);
        JPanel racine = new JPanel(new BorderLayout());
        racine.add(fond, BorderLayout.CENTER);
        return racine;
    }

    /** Suivi client sans authentification (EF-23). */
    private void ouvrirSuivi() {
        javax.swing.JDialog d = new javax.swing.JDialog(this, "Suivi de réparation", true);
        d.setContentPane(new ma.phonerepair.presentation.client.SuiviReparationPanel());
        d.setSize(900, 480);
        d.setLocationRelativeTo(this);
        d.setVisible(true);
    }

    private String nomBoutique() {
        try {
            return boutiqueService.obtenir().map(Boutique::getNom).orElse("Boutique non configurée");
        } catch (RuntimeException e) {
            return "Base de données indisponible";
        }
    }

    private void seConnecter() {
        String username = champUsername.getText();
        char[] motDePasse = champMotDePasse.getPassword();
        Utilisateur[] connecte = new Utilisateur[1];
        boolean ok = UiUtil.executer(this, () -> connecte[0] = authService.connecter(username, motDePasse));
        champMotDePasse.setText("");
        if (ok) {
            new DashboardFrame(connecte[0]).setVisible(true);
            dispose();
        } else {
            champMotDePasse.requestFocusInWindow();
        }
    }
}
