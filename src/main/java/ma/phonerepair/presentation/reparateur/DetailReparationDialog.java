package ma.phonerepair.presentation.reparateur;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import ma.phonerepair.model.Appareil;
import ma.phonerepair.model.HistoriqueStatut;
import ma.phonerepair.model.Intervention;
import ma.phonerepair.model.Reparation;
import ma.phonerepair.model.SituationFinanciere;
import ma.phonerepair.model.Utilisateur;
import ma.phonerepair.model.enums.StatutReparation;
import ma.phonerepair.presentation.UiTheme;
import ma.phonerepair.presentation.UiUtil;
import ma.phonerepair.service.AppareilService;
import ma.phonerepair.service.PaiementService;
import ma.phonerepair.service.ReparationService;
import ma.phonerepair.util.DateUtil;
import ma.phonerepair.util.MoneyUtil;

/**
 * Détail d'une réparation : appareils, statuts, interventions, situation de paiement.
 * Point d'entrée des actions EF-09, EF-10, EF-11.
 */
public class DetailReparationDialog extends JDialog {

    private final ReparationService reparationService = new ReparationService();
    private final AppareilService appareilService = new AppareilService();
    private final PaiementService paiementService = new PaiementService();
    private final Utilisateur acteur;
    private final int reparationId;

    private Reparation reparation;
    private final JLabel labelTitre = UiTheme.titre("");
    private final JLabel labelInfos = new JLabel();
    private final JLabel labelStatut = new JLabel();
    private final JLabel labelMontants = new JLabel();
    private final AppareilsTableModel modele = new AppareilsTableModel();
    private final JTable table = new JTable(modele);

    public DetailReparationDialog(Window parent, Utilisateur acteur, int reparationId) {
        super(parent, "Détail de la réparation", ModalityType.APPLICATION_MODAL);
        this.acteur = acteur;
        this.reparationId = reparationId;

        JPanel racine = new JPanel(new BorderLayout(0, 10));
        racine.setBackground(UiTheme.FOND);
        racine.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        JPanel entete = new JPanel(new GridLayout(0, 1, 0, 4));
        entete.setOpaque(false);
        entete.add(labelTitre);
        entete.add(labelInfos);
        labelStatut.setFont(UiTheme.POLICE_GRAS);
        entete.add(labelStatut);
        labelMontants.setFont(UiTheme.SOUS_TITRE);
        entete.add(labelMontants);
        racine.add(entete, BorderLayout.NORTH);

        UiTheme.styliserTable(table);
        table.setAutoCreateRowSorter(false);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(AppareilsTableModel.COL_STATUT).setCellRenderer(UiTheme.rendreStatut());
        table.getColumnModel().getColumn(AppareilsTableModel.COL_MONTANT).setCellRenderer(UiTheme.rendreMontant());
        racine.add(new JScrollPane(table), BorderLayout.CENTER);
        racine.add(construireBoutons(), BorderLayout.SOUTH);

        setContentPane(racine);
        setSize(new Dimension(980, 520));
        setLocationRelativeTo(parent);
        if (!recharger()) {
            dispose();
        }
    }

    private JPanel construireBoutons() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        p.setOpaque(false);
        JButton intervention = UiTheme.boutonPrincipal("Diagnostic / coûts…");
        intervention.addActionListener(e -> saisirIntervention());
        JButton statut = UiTheme.boutonAccent("Changer le statut…");
        statut.addActionListener(e -> changerStatut());
        JButton encaisser = UiTheme.boutonPrincipal("Encaisser…");
        encaisser.addActionListener(e -> encaisser());
        encaisser.setVisible(acteur.aProfilReparateur());
        JButton historique = UiTheme.boutonSecondaire("Historique");
        historique.addActionListener(e -> afficherHistorique());
        JButton ajouter = UiTheme.boutonSecondaire("Ajouter un appareil…");
        ajouter.addActionListener(e -> ajouterAppareil());
        JButton fermer = UiTheme.boutonSecondaire("Fermer");
        fermer.addActionListener(e -> dispose());
        p.add(intervention);
        p.add(statut);
        p.add(encaisser);
        p.add(historique);
        p.add(ajouter);
        p.add(fermer);
        return p;
    }

    /** @return false si la réparation n'a pas pu être chargée */
    private boolean recharger() {
        Reparation r = UiUtil.appeler(this, () -> reparationService.obtenir(acteur, reparationId));
        SituationFinanciere sf = UiUtil.appeler(this, () -> paiementService.situation(acteur, reparationId));
        if (r == null || sf == null) {
            return false;
        }
        reparation = r;
        labelTitre.setText(r.getCodeSuivi());
        labelInfos.setText(r.getClient().getNomComplet() + " · " + r.getClient().getTelephone()
                + " · déposé le " + DateUtil.formater(r.getDateCreation()) + " · responsable : " + r.getNomReparateur()
                + (r.getRemarque() == null ? "" : " · « " + r.getRemarque() + " »"));
        var global = ReparationService.calculerStatutGlobal(r.getAppareils().stream().map(Appareil::getStatut).toList());
        labelStatut.setText("Statut global : " + global.getLibelle());
        labelStatut.setForeground(UiTheme.couleurStatut(global.name()));
        labelMontants.setText("Total " + MoneyUtil.formater(sf.getTotal()) + "   ·   Payé " + MoneyUtil.formater(sf.getPaye())
                + "   ·   Reste " + MoneyUtil.formater(sf.getReste()) + "   (" + sf.getStatut().getLibelle() + ")");
        modele.remplacer(r.getAppareils());
        return true;
    }

    private Appareil selection() {
        int l = table.getSelectedRow();
        if (l < 0) {
            if (modele.getRowCount() == 1) {
                return modele.get(0);
            }
            UiUtil.avertir(this, "Sélectionnez d'abord un appareil dans le tableau.");
            return null;
        }
        return modele.get(l);
    }

    private void saisirIntervention() {
        Appareil a = selection();
        if (a == null) {
            return;
        }
        if (new InterventionDialog(this, acteur, a).afficher()) {
            recharger();
        }
    }

    private void changerStatut() {
        Appareil a = selection();
        if (a == null) {
            return;
        }
        List<StatutReparation> possibles = new ArrayList<>(a.getStatut().suivantsAutorises());
        if (possibles.isEmpty()) {
            UiUtil.avertir(this, "Cet appareil est " + a.getStatut().getLibelle().toLowerCase() + " : statut définitif.");
            return;
        }
        JComboBox<StatutReparation> choix = new JComboBox<>(possibles.toArray(new StatutReparation[0]));
        JTextField commentaire = new JTextField(24);
        JPanel p = new JPanel(new GridLayout(0, 1, 4, 4));
        p.add(new JLabel(a.getDesignation() + " — actuellement : " + a.getStatut().getLibelle()));
        p.add(new JLabel("Nouveau statut"));
        p.add(choix);
        p.add(new JLabel("Commentaire (facultatif)"));
        p.add(commentaire);
        if (JOptionPane.showConfirmDialog(this, p, "Changer le statut", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return;
        }
        StatutReparation nouveau = (StatutReparation) choix.getSelectedItem();
        if (nouveau == StatutReparation.CANCELLED
                && !UiUtil.confirmer(this, "Annuler définitivement la réparation de cet appareil ?")) {
            return;
        }
        if (UiUtil.executer(this, () -> appareilService.changerStatut(acteur, a.getId(), nouveau, commentaire.getText()))) {
            recharger();
        }
    }

    /** Ouvre l'encaissement pré-rempli avec le code de cette réparation (EF-13). */
    private void encaisser() {
        JDialog d = new JDialog(this, "Encaissement — " + reparation.getCodeSuivi(), ModalityType.APPLICATION_MODAL);
        d.setContentPane(new PaiementPanel(acteur, reparation.getCodeSuivi()));
        d.setSize(900, 520);
        d.setLocationRelativeTo(this);
        d.setVisible(true);
        recharger();
    }

    private void afficherHistorique() {
        Appareil a = selection();
        if (a == null) {
            return;
        }
        List<HistoriqueStatut> h = UiUtil.appeler(this, () -> appareilService.historique(acteur, a.getId()));
        if (h == null) {
            return;
        }
        DefaultTableModel m = new DefaultTableModel(new String[] {"Date", "De", "Vers", "Par", "Commentaire"}, 0);
        for (HistoriqueStatut x : h) {
            m.addRow(new Object[] {DateUtil.formater(x.getDateChangement()),
                    x.getAncienStatut() == null ? "—" : x.getAncienStatut().getLibelle(),
                    x.getNouveauStatut().getLibelle(), x.getNomUtilisateur(),
                    x.getCommentaire() == null ? "" : x.getCommentaire()});
        }
        JTable t = new JTable(m);
        UiTheme.styliserTable(t);
        JScrollPane sp = new JScrollPane(t);
        sp.setPreferredSize(new Dimension(700, 220));
        JOptionPane.showMessageDialog(this, sp, "Historique — " + a.getDesignation(), JOptionPane.PLAIN_MESSAGE);
    }

    private void ajouterAppareil() {
        AppareilForm form = new AppareilForm();
        if (JOptionPane.showConfirmDialog(this, form, "Ajouter un appareil à " + reparation.getCodeSuivi(),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return;
        }
        if (UiUtil.executer(this, () -> reparationService.ajouterAppareil(acteur, reparationId, form.lire()))) {
            recharger();
        }
    }

    /** Modèle du tableau des appareils. */
    private static class AppareilsTableModel extends AbstractTableModel {
        static final int COL_STATUT = 3;
        static final int COL_MONTANT = 4;
        private static final String[] COLONNES = {"Appareil", "IMEI / série", "Panne déclarée", "Statut", "Montant",
            "Diagnostic"};
        private final List<Appareil> lignes = new ArrayList<>();

        void remplacer(List<Appareil> l) {
            lignes.clear();
            lignes.addAll(l);
            fireTableDataChanged();
        }

        Appareil get(int i) {
            return lignes.get(i);
        }

        @Override
        public int getRowCount() {
            return lignes.size();
        }

        @Override
        public int getColumnCount() {
            return COLONNES.length;
        }

        @Override
        public String getColumnName(int c) {
            return COLONNES[c];
        }

        @Override
        public Object getValueAt(int l, int c) {
            Appareil a = lignes.get(l);
            return switch (c) {
                case 0 -> a.getDesignation();
                case 1 -> a.getImeiSerie() == null ? "" : a.getImeiSerie();
                case 2 -> a.getProblemeSignale();
                case COL_STATUT -> a.getStatut();
                case COL_MONTANT -> a.getInterventions().isEmpty() ? "à chiffrer"
                        : MoneyUtil.formater(a.getInterventions().stream().map(Intervention::getMontantClient)
                                .reduce(BigDecimal.ZERO, BigDecimal::add));
                case 5 -> a.getInterventions().isEmpty() ? ""
                        : a.getInterventions().get(a.getInterventions().size() - 1).getDiagnostic();
                default -> "";
            };
        }
    }
}
