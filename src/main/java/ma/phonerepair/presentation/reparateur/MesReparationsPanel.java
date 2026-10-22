package ma.phonerepair.presentation.reparateur;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import ma.phonerepair.model.ResumeReparation;
import ma.phonerepair.model.Utilisateur;
import ma.phonerepair.model.enums.StatutGlobal;
import ma.phonerepair.presentation.UiTheme;
import ma.phonerepair.presentation.UiUtil;
import ma.phonerepair.service.ReparationService;
import ma.phonerepair.util.DateUtil;
import ma.phonerepair.util.MoneyUtil;

/**
 * Liste des réparations (EF-12) : les siennes pour un réparateur, toutes pour le propriétaire.
 * Recherche par code / nom / téléphone, filtre par statut global, double-clic pour ouvrir.
 */
public class MesReparationsPanel extends JPanel {

    private static final String TOUS = "Tous les statuts";

    private final ReparationService reparationService = new ReparationService();
    private final Utilisateur acteur;

    private final JTextField champRecherche = new JTextField(18);
    private final JComboBox<Object> filtreStatut = new JComboBox<>();
    private final ResumeTableModel modele;
    private final JTable table;

    public MesReparationsPanel(Utilisateur acteur) {
        super(new BorderLayout(0, 10));
        this.acteur = acteur;
        this.modele = new ResumeTableModel(acteur.estProprietaire());
        this.table = new JTable(modele);
        setBackground(UiTheme.FOND);
        setBorder(BorderFactory.createEmptyBorder(20, 28, 20, 28));

        JPanel entete = new JPanel(new BorderLayout(0, 8));
        entete.setOpaque(false);
        entete.add(UiTheme.titre(acteur.estProprietaire() ? "Réparations" : "Mes réparations"), BorderLayout.NORTH);
        entete.add(construireFiltres(), BorderLayout.SOUTH);
        add(entete, BorderLayout.NORTH);

        UiTheme.styliserTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(ResumeTableModel.COL_STATUT).setCellRenderer(UiTheme.rendreStatut());
        for (int c : new int[] {ResumeTableModel.COL_TOTAL, ResumeTableModel.COL_TOTAL + 1, ResumeTableModel.COL_TOTAL + 2}) {
            table.getColumnModel().getColumn(c).setCellRenderer(UiTheme.rendreMontant());
        }
        int[] largeurs = {150, 130, 170, 110, 80, 110, 100, 100, 100, 140};
        for (int c = 0; c < table.getColumnCount(); c++) {
            table.getColumnModel().getColumn(c).setPreferredWidth(largeurs[c]);
        }
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    ouvrir();
                }
            }
        });
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bas = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        bas.setOpaque(false);
        JButton ouvrir = UiTheme.boutonPrincipal("Ouvrir la réparation");
        ouvrir.addActionListener(e -> ouvrir());
        bas.add(ouvrir);
        bas.add(UiTheme.aide("Double-cliquez sur une ligne pour voir les appareils, saisir l'intervention ou changer un statut."));
        add(bas, BorderLayout.SOUTH);

        // Rechargement à chaque affichage (ex. après une nouvelle réparation)
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                charger();
            }
        });
    }

    private JPanel construireFiltres() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        p.setOpaque(false);
        filtreStatut.addItem(TOUS);
        for (StatutGlobal s : StatutGlobal.values()) {
            filtreStatut.addItem(s);
        }
        filtreStatut.addActionListener(e -> charger());
        champRecherche.addActionListener(e -> charger());
        JButton chercher = UiTheme.boutonSecondaire("Rechercher");
        chercher.addActionListener(e -> charger());
        p.add(new JLabel("Code, client ou téléphone :"));
        p.add(champRecherche);
        p.add(filtreStatut);
        p.add(chercher);
        return p;
    }

    public void charger() {
        Object choix = filtreStatut.getSelectedItem();
        StatutGlobal statut = choix instanceof StatutGlobal s ? s : null;
        List<ResumeReparation> liste = UiUtil.appeler(this,
                () -> reparationService.lister(acteur, champRecherche.getText(), statut));
        if (liste != null) {
            modele.remplacer(liste);
        }
    }

    private void ouvrir() {
        int ligne = table.getSelectedRow();
        if (ligne < 0) {
            UiUtil.avertir(this, "Sélectionnez une réparation.");
            return;
        }
        ResumeReparation r = modele.get(table.convertRowIndexToModel(ligne));
        new DetailReparationDialog(SwingUtilities.getWindowAncestor(this), acteur, r.getId()).setVisible(true);
        charger();
    }

    /** Modèle du tableau des réparations. */
    private static class ResumeTableModel extends AbstractTableModel {
        static final int COL_STATUT = 5;
        static final int COL_TOTAL = 6;
        private final List<String> colonnes = new ArrayList<>(List.of(
                "Code", "Date", "Client", "Téléphone", "Appareils", "Statut", "Total", "Payé", "Reste"));
        private final boolean afficherReparateur;
        private final List<ResumeReparation> lignes = new ArrayList<>();

        ResumeTableModel(boolean afficherReparateur) {
            this.afficherReparateur = afficherReparateur;
            if (afficherReparateur) {
                colonnes.add("Réparateur");
            }
        }

        void remplacer(List<ResumeReparation> l) {
            lignes.clear();
            lignes.addAll(l);
            fireTableDataChanged();
        }

        ResumeReparation get(int i) {
            return lignes.get(i);
        }

        @Override
        public int getRowCount() {
            return lignes.size();
        }

        @Override
        public int getColumnCount() {
            return colonnes.size();
        }

        @Override
        public String getColumnName(int c) {
            return colonnes.get(c);
        }

        @Override
        public Object getValueAt(int l, int c) {
            ResumeReparation r = lignes.get(l);
            return switch (c) {
                case 0 -> r.getCodeSuivi();
                case 1 -> DateUtil.formater(r.getDateCreation().toLocalDate());
                case 2 -> r.getClient().getNomComplet();
                case 3 -> r.getClient().getTelephone();
                case 4 -> r.getNombreAppareils();
                case COL_STATUT -> r.getStatutGlobal();
                case COL_TOTAL -> MoneyUtil.formater(r.getSituation().getTotal());
                case 7 -> MoneyUtil.formater(r.getSituation().getPaye());
                case 8 -> MoneyUtil.formater(r.getSituation().getReste());
                case 9 -> afficherReparateur ? r.getNomReparateur() : "";
                default -> "";
            };
        }
    }
}
