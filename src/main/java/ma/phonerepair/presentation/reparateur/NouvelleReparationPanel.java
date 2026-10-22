package ma.phonerepair.presentation.reparateur;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import ma.phonerepair.model.Appareil;
import ma.phonerepair.model.Client;
import ma.phonerepair.model.Reparation;
import ma.phonerepair.model.Utilisateur;
import ma.phonerepair.presentation.UiTheme;
import ma.phonerepair.presentation.UiUtil;
import ma.phonerepair.service.ClientService;
import ma.phonerepair.service.ReparationService;

/**
 * Création d'une réparation (EF-07, EF-08, scénario S3) :
 * 1. choisir ou créer le client, 2. ajouter un ou plusieurs appareils, 3. enregistrer → code de suivi.
 */
public class NouvelleReparationPanel extends JPanel {

    private final ClientService clientService = new ClientService();
    private final ReparationService reparationService = new ReparationService();
    private final Utilisateur acteur;
    private final Runnable apresCreation;

    private Client clientChoisi;
    private final JLabel labelClient = new JLabel("Aucun client sélectionné");
    private final JTextField champRecherche = new JTextField(18);
    private final DefaultListModel<Client> resultats = new DefaultListModel<>();
    private final JList<Client> listeClients = new JList<>(resultats);

    private final AppareilForm formAppareil = new AppareilForm();
    private final List<Appareil> appareils = new ArrayList<>();
    private final DefaultTableModel modeleAppareils = new DefaultTableModel(
            new String[] {"Marque", "Modèle", "IMEI", "Panne déclarée"}, 0) {
        @Override
        public boolean isCellEditable(int l, int c) {
            return false;
        }
    };
    private final JTable tableAppareils = new JTable(modeleAppareils);
    private final JTextField champRemarque = new JTextField(40);

    public NouvelleReparationPanel(Utilisateur acteur, Runnable apresCreation) {
        super(new BorderLayout(0, 10));
        this.acteur = acteur;
        this.apresCreation = apresCreation;
        setBackground(UiTheme.FOND);
        setBorder(BorderFactory.createEmptyBorder(20, 28, 20, 28));

        JPanel entete = new JPanel(new BorderLayout());
        entete.setOpaque(false);
        entete.add(UiTheme.titre("Nouvelle réparation"), BorderLayout.NORTH);
        entete.add(UiTheme.aide("Un seul code de suivi sera remis au client, quel que soit le nombre d'appareils."),
                BorderLayout.SOUTH);
        add(entete, BorderLayout.NORTH);

        JPanel corps = new PanneauLargeurFixe();
        corps.setOpaque(false);
        corps.setLayout(new BoxLayout(corps, BoxLayout.Y_AXIS));
        corps.add(carte("1. Client", construireClient()));
        corps.add(carte("2. Appareils", construireAppareils()));
        JScrollPane defilementCorps = new JScrollPane(corps);
        defilementCorps.setBorder(null);
        defilementCorps.getViewport().setBackground(UiTheme.FOND);
        defilementCorps.getVerticalScrollBar().setUnitIncrement(16);
        defilementCorps.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(defilementCorps, BorderLayout.CENTER);
        add(construireBas(), BorderLayout.SOUTH);
    }

    private static JPanel carte(String titre, JPanel contenu) {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setBackground(UiTheme.BLANC);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(6, 0, 6, 0),
                        BorderFactory.createLineBorder(UiTheme.BORDURE)),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)));
        p.add(UiTheme.sousTitre(titre), BorderLayout.NORTH);
        p.add(contenu, BorderLayout.CENTER);
        return p;
    }

    private JPanel construireClient() {
        JPanel p = new JPanel(new BorderLayout(10, 6));
        p.setOpaque(false);

        JPanel ligne = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        ligne.setOpaque(false);
        ligne.add(new JLabel("Téléphone ou nom :"));
        ligne.add(champRecherche);
        JButton rechercher = UiTheme.boutonSecondaire("Rechercher");
        rechercher.addActionListener(e -> rechercherClient());
        champRecherche.addActionListener(e -> rechercherClient());
        JButton nouveau = UiTheme.boutonSecondaire("Nouveau client");
        nouveau.addActionListener(e -> nouveauClient());
        ligne.add(rechercher);
        ligne.add(nouveau);
        p.add(ligne, BorderLayout.NORTH);

        listeClients.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listeClients.setVisibleRowCount(3);
        listeClients.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && listeClients.getSelectedValue() != null) {
                choisirClient(listeClients.getSelectedValue());
            }
        });
        JScrollPane defilement = new JScrollPane(listeClients);
        defilement.setPreferredSize(new Dimension(380, 62));
        p.add(defilement, BorderLayout.CENTER);

        labelClient.setFont(UiTheme.POLICE_GRAS);
        labelClient.setForeground(UiTheme.TEXTE_SECONDAIRE);
        p.add(labelClient, BorderLayout.SOUTH);
        return p;
    }

    private JPanel construireAppareils() {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setOpaque(false);
        JPanel haut = new JPanel(new BorderLayout());
        haut.setOpaque(false);
        haut.add(formAppareil, BorderLayout.CENTER);
        JPanel boutons = new JPanel(new GridLayout(0, 1, 0, 6));
        boutons.setOpaque(false);
        JButton ajouter = UiTheme.boutonAccent("Ajouter l'appareil ↓");
        ajouter.addActionListener(e -> ajouterAppareil());
        JButton retirer = UiTheme.boutonSecondaire("Retirer la ligne");
        retirer.addActionListener(e -> retirerAppareil());
        boutons.add(ajouter);
        boutons.add(retirer);
        JPanel conteneurBoutons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        conteneurBoutons.setOpaque(false);
        conteneurBoutons.add(boutons);
        haut.add(conteneurBoutons, BorderLayout.EAST);
        p.add(haut, BorderLayout.NORTH);

        UiTheme.styliserTable(tableAppareils);
        JScrollPane defilement = new JScrollPane(tableAppareils);
        defilement.setPreferredSize(new Dimension(600, 96));
        p.add(defilement, BorderLayout.CENTER);
        return p;
    }

    private JPanel construireBas() {
        JPanel p = new JPanel(new BorderLayout(8, 0));
        p.setOpaque(false);
        p.add(new JLabel("Remarque :"), BorderLayout.WEST);
        p.add(champRemarque, BorderLayout.CENTER);
        JButton enregistrer = UiTheme.boutonPrincipal("Enregistrer la réparation");
        enregistrer.addActionListener(e -> enregistrer());
        p.add(enregistrer, BorderLayout.EAST);
        return p;
    }

    private void rechercherClient() {
        List<Client> trouves = UiUtil.appeler(this, () -> clientService.rechercher(acteur, champRecherche.getText()));
        if (trouves == null) {
            return;
        }
        resultats.clear();
        trouves.forEach(resultats::addElement);
        if (trouves.isEmpty()) {
            UiUtil.avertir(this, "Aucun client trouvé. Cliquez sur « Nouveau client ».");
        } else if (trouves.size() == 1) {
            listeClients.setSelectedIndex(0);
        }
    }

    private void nouveauClient() {
        JTextField nom = new JTextField(22);
        JTextField tel = new JTextField(champRecherche.getText().matches("[0-9+ .\\-]+") ? champRecherche.getText() : "", 22);
        JPanel form = new JPanel(new GridLayout(0, 1, 4, 4));
        form.add(new JLabel("Nom complet *"));
        form.add(nom);
        form.add(new JLabel("Téléphone *"));
        form.add(tel);
        if (JOptionPane.showConfirmDialog(this, form, "Nouveau client", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return;
        }
        // RG-09 : proposer le client existant plutôt que créer un doublon
        List<Client> existants = UiUtil.appeler(this, () -> clientService.trouverParTelephone(acteur, tel.getText()));
        if (existants != null && !existants.isEmpty()) {
            Client existant = existants.get(0);
            int choix = JOptionPane.showConfirmDialog(this,
                    "Ce numéro appartient déjà à « " + existant.getNomComplet() + " ».\nUtiliser ce client ?",
                    "Client existant", JOptionPane.YES_NO_CANCEL_OPTION);
            if (choix == JOptionPane.YES_OPTION) {
                choisirClient(existant);
                return;
            }
            if (choix != JOptionPane.NO_OPTION) {
                return;
            }
        }
        Client cree = UiUtil.appeler(this, () -> clientService.creer(acteur, nom.getText(), tel.getText()));
        if (cree != null) {
            resultats.clear();
            resultats.addElement(cree);
            choisirClient(cree);
        }
    }

    private void choisirClient(Client c) {
        clientChoisi = c;
        labelClient.setText("✔ " + c.getNomComplet() + " — " + c.getTelephone());
        labelClient.setForeground(UiTheme.SUCCES);
    }

    private void ajouterAppareil() {
        Appareil a = formAppareil.lire();
        // Validation immédiate (règles du service) pour signaler l'erreur sur la ligne en cours
        if (!UiUtil.executer(this, () -> ReparationService.validerAppareil(a))) {
            return;
        }
        appareils.add(a);
        modeleAppareils.addRow(new Object[] {a.getMarque().trim(), a.getModele().trim(),
                a.getImeiSerie() == null ? "" : a.getImeiSerie().trim(), a.getProblemeSignale().trim()});
        formAppareil.vider();
    }

    private void retirerAppareil() {
        int ligne = tableAppareils.getSelectedRow();
        if (ligne >= 0) {
            int index = tableAppareils.convertRowIndexToModel(ligne);
            appareils.remove(index);
            modeleAppareils.removeRow(index);
        }
    }

    private void enregistrer() {
        Reparation r = UiUtil.appeler(this,
                () -> reparationService.creer(acteur, clientChoisi, appareils, champRemarque.getText()));
        if (r == null) {
            return;
        }
        afficherCode(r);
        reinitialiser();
        apresCreation.run();
    }

    private void afficherCode(Reparation r) {
        JPanel p = new JPanel(new GridLayout(0, 1, 4, 4));
        p.add(new JLabel("Réparation enregistrée pour " + r.getClient().getNomComplet()
                + " (" + r.getAppareils().size() + " appareil(s))."));
        p.add(new JLabel("Code de suivi à remettre au client :"));
        JTextField code = new JTextField(r.getCodeSuivi());
        code.setEditable(false);
        code.setFont(new Font(UiTheme.TITRE.getFamily(), Font.BOLD, 26));
        code.setForeground(UiTheme.PRIMAIRE);
        p.add(code);
        p.add(UiTheme.aide("Le client suit sa réparation avec ce code + les 4 derniers chiffres de son téléphone."));
        JOptionPane.showMessageDialog(this, p, "Code de suivi", JOptionPane.INFORMATION_MESSAGE);
    }

    private void reinitialiser() {
        clientChoisi = null;
        labelClient.setText("Aucun client sélectionné");
        labelClient.setForeground(UiTheme.TEXTE_SECONDAIRE);
        resultats.clear();
        champRecherche.setText("");
        appareils.clear();
        modeleAppareils.setRowCount(0);
        champRemarque.setText("");
        formAppareil.vider();
    }

    /** Panneau qui suit la largeur de la zone de défilement (pas de barre horizontale). */
    private static class PanneauLargeurFixe extends JPanel implements javax.swing.Scrollable {
        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(java.awt.Rectangle r, int orientation, int direction) {
            return 16;
        }

        @Override
        public int getScrollableBlockIncrement(java.awt.Rectangle r, int orientation, int direction) {
            return 100;
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }
}
