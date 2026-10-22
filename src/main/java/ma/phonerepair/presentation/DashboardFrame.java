package ma.phonerepair.presentation;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import ma.phonerepair.model.Boutique;
import ma.phonerepair.model.Utilisateur;
import ma.phonerepair.presentation.client.SuiviReparationPanel;
import ma.phonerepair.presentation.proprietaire.BoutiquePanel;
import ma.phonerepair.presentation.proprietaire.CaissesPanel;
import ma.phonerepair.presentation.proprietaire.CreditsPanel;
import ma.phonerepair.presentation.proprietaire.StatistiquesPanel;
import ma.phonerepair.presentation.proprietaire.ReparateursPanel;
import ma.phonerepair.presentation.reparateur.MaCaissePanel;
import ma.phonerepair.presentation.reparateur.MesReparationsPanel;
import ma.phonerepair.presentation.reparateur.NouvelleReparationPanel;
import ma.phonerepair.presentation.reparateur.PaiementPanel;
import ma.phonerepair.service.AuthService;
import ma.phonerepair.service.BoutiqueService;

/**
 * Fenêtre principale : en-tête, menu latéral selon le rôle, zone centrale en CardLayout.
 * <p>
 * Pour ajouter un écran : créer le panel puis l'enregistrer dans {@link #construireMenus()}.
 */
public class DashboardFrame extends JFrame {

    private final AuthService authService = new AuthService();
    private final BoutiqueService boutiqueService = new BoutiqueService();

    private Utilisateur utilisateur;
    private final CardLayout cartes = new CardLayout();
    private final JPanel zoneCentrale = new JPanel(cartes);
    private final JPanel menu = new JPanel();
    private final List<UiTheme.BoutonPlat> boutonsMenu = new ArrayList<>();
    private final JLabel labelBoutique = new JLabel();

    public DashboardFrame(Utilisateur utilisateur) {
        super("RepairTrack");
        this.utilisateur = utilisateur;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1100, 700));

        JPanel racine = new JPanel(new BorderLayout());
        racine.add(construireEntete(), BorderLayout.NORTH);
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setBackground(UiTheme.PRIMAIRE);
        menu.setPreferredSize(new Dimension(220, 0));
        racine.add(menu, BorderLayout.WEST);
        zoneCentrale.setBackground(UiTheme.FOND);
        racine.add(zoneCentrale, BorderLayout.CENTER);
        setContentPane(racine);

        construireMenus();
        setLocationRelativeTo(null);
        setExtendedState(MAXIMIZED_BOTH);   // plein écran pour les tableaux
    }

    private JComponent construireEntete() {
        JPanel entete = new JPanel(new BorderLayout());
        entete.setBackground(UiTheme.BLANC);
        entete.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UiTheme.BORDURE),
                BorderFactory.createEmptyBorder(10, 18, 10, 18)));

        labelBoutique.setFont(UiTheme.SOUS_TITRE);
        labelBoutique.setForeground(UiTheme.PRIMAIRE);
        entete.add(labelBoutique, BorderLayout.WEST);

        JPanel droite = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        droite.setOpaque(false);
        JLabel qui = new JLabel(utilisateur.getNomComplet() + " · " + utilisateur.getRole().getLibelle());
        qui.setForeground(UiTheme.TEXTE_SECONDAIRE);
        JButton motDePasse = UiTheme.boutonSecondaire("Mot de passe");
        motDePasse.addActionListener(e -> changerMotDePasse());
        JButton deconnexion = UiTheme.boutonSecondaire("Déconnexion");
        deconnexion.addActionListener(e -> deconnecter());
        droite.add(qui);
        droite.add(motDePasse);
        droite.add(deconnexion);
        entete.add(droite, BorderLayout.EAST);
        return entete;
    }

    /** (Re)construit le menu et les écrans selon les droits de l'utilisateur (matrice RG-05). */
    private void construireMenus() {
        menu.removeAll();
        zoneCentrale.removeAll();
        boutonsMenu.clear();
        rafraichirNomBoutique();

        menu.add(Box.createVerticalStrut(12));
        ajouterEcran("Accueil", new AccueilPanel(utilisateur));

        ajouterTitreSection("RÉPARATIONS");
        if (utilisateur.aProfilReparateur()) {
            ajouterEcran("Nouvelle réparation",
                    new NouvelleReparationPanel(utilisateur, () -> afficher(nomListeReparations())));
        }
        ajouterEcran(nomListeReparations(), new MesReparationsPanel(utilisateur));
        if (utilisateur.aProfilReparateur()) {
            ajouterEcran("Encaissement", new PaiementPanel(utilisateur, null));
            ajouterEcran("Ma caisse", new MaCaissePanel(utilisateur));
        }
        ajouterEcran("Suivi client", new SuiviReparationPanel());

        if (utilisateur.estProprietaire()) {
            ajouterTitreSection("PROPRIÉTAIRE");
            ajouterEcran("Boutique", new BoutiquePanel(utilisateur, this::rafraichirNomBoutique));
            ajouterEcran("Réparateurs", new ReparateursPanel(utilisateur, this::utilisateurModifie));
            ajouterEcran("Caisses", new CaissesPanel(utilisateur));
            ajouterEcran("Statistiques", new StatistiquesPanel(utilisateur));
            ajouterEcran("Crédits boutiques", new CreditsPanel(utilisateur));
        }

        menu.add(Box.createVerticalGlue());
        menu.revalidate();
        menu.repaint();
        afficher("Accueil");
    }

    private String nomListeReparations() {
        return utilisateur.estProprietaire() ? "Toutes les réparations" : "Mes réparations";
    }

    private void ajouterTitreSection(String titre) {
        JLabel l = new JLabel(titre);
        l.setForeground(new java.awt.Color(0xA0AEC0));
        l.setFont(UiTheme.POLICE.deriveFont(11f));
        l.setBorder(BorderFactory.createEmptyBorder(16, 18, 4, 0));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        menu.add(l);
    }

    private void ajouterEcran(String nom, JComponent ecran) {
        zoneCentrale.add(ecran, nom);
        UiTheme.BoutonPlat bouton = (UiTheme.BoutonPlat) UiTheme.boutonMenu(nom);
        bouton.setAlignmentX(Component.LEFT_ALIGNMENT);
        bouton.setName(nom);
        bouton.addActionListener(e -> afficher(nom));
        boutonsMenu.add(bouton);
        menu.add(bouton);
    }

    private void afficher(String nom) {
        cartes.show(zoneCentrale, nom);
        for (UiTheme.BoutonPlat b : boutonsMenu) {
            b.setActif(nom.equals(b.getName()));
        }
    }

    private void rafraichirNomBoutique() {
        String nom;
        try {
            nom = boutiqueService.obtenir().map(Boutique::getNom).orElse("Boutique non configurée");
        } catch (RuntimeException e) {
            nom = "RepairTrack";
        }
        labelBoutique.setText(nom);
    }

    /** Appelé quand le propriétaire active son profil réparateur : les menus changent. */
    private void utilisateurModifie(Utilisateur nouveau) {
        this.utilisateur = nouveau;
        construireMenus();
        afficher("Réparateurs");
    }

    private void changerMotDePasse() {
        JPasswordField ancien = new JPasswordField();
        JPasswordField nouveau = new JPasswordField();
        JPasswordField confirmation = new JPasswordField();
        JPanel p = new JPanel(new GridLayout(0, 1, 4, 4));
        p.add(new JLabel("Mot de passe actuel"));
        p.add(ancien);
        p.add(new JLabel("Nouveau mot de passe (6 caractères min.)"));
        p.add(nouveau);
        p.add(new JLabel("Confirmation"));
        p.add(confirmation);
        int choix = JOptionPane.showConfirmDialog(this, p, "Changer mon mot de passe",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (choix != JOptionPane.OK_OPTION) {
            return;
        }
        if (!java.util.Arrays.equals(nouveau.getPassword(), confirmation.getPassword())) {
            UiUtil.avertir(this, "La confirmation ne correspond pas au nouveau mot de passe.");
            return;
        }
        if (UiUtil.executer(this, () ->
                authService.changerMotDePasse(utilisateur, ancien.getPassword(), nouveau.getPassword()))) {
            UiUtil.succes(this, "Mot de passe modifié.");
        }
    }

    private void deconnecter() {
        if (UiUtil.confirmer(this, "Se déconnecter ?")) {
            dispose();
            new LoginFrame().setVisible(true);
        }
    }
}
