package ma.phonerepair.presentation.proprietaire;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import ma.phonerepair.model.MouvementCaisse;
import ma.phonerepair.model.ResumeCaisse;
import ma.phonerepair.model.Utilisateur;
import ma.phonerepair.model.enums.TypeMouvement;
import ma.phonerepair.presentation.MouvementsTableModel;
import ma.phonerepair.presentation.UiTheme;
import ma.phonerepair.presentation.UiUtil;
import ma.phonerepair.service.CaisseService;
import ma.phonerepair.util.MoneyUtil;

/**
 * Vue propriétaire de toutes les caisses (EF-17) : soldes, commissions, versements reçus
 * et paiement des commissions (EF-18).
 */
public class CaissesPanel extends JPanel {

    private final CaisseService caisseService = new CaisseService();
    private final Utilisateur acteur;
    private final CaissesTableModel modele = new CaissesTableModel();
    private final JTable table = new JTable(modele);
    private final JLabel labelTotal = UiTheme.sousTitre(" ");

    public CaissesPanel(Utilisateur acteur) {
        super(new BorderLayout(0, 12));
        this.acteur = acteur;
        setBackground(UiTheme.FOND);
        setBorder(BorderFactory.createEmptyBorder(20, 28, 20, 28));

        JPanel haut = new JPanel(new GridLayout(0, 1, 0, 4));
        haut.setOpaque(false);
        haut.add(UiTheme.titre("Caisses des réparateurs"));
        haut.add(UiTheme.aide("Une caisse par réparateur. Solde = encaissements − achats de pièces − versements − commissions payées."));
        haut.add(labelTotal);
        add(haut, BorderLayout.NORTH);

        UiTheme.styliserTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(0).setPreferredWidth(170);
        for (int c = 1; c < modele.getColumnCount(); c++) {
            table.getColumnModel().getColumn(c).setCellRenderer(UiTheme.rendreMontant());
        }
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bas = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        bas.setOpaque(false);
        JButton mouvements = UiTheme.boutonSecondaire("Voir les mouvements");
        mouvements.addActionListener(e -> voirMouvements());
        JButton versement = UiTheme.boutonPrincipal("Versement reçu…");
        versement.addActionListener(e -> sortie(TypeMouvement.VERSEMENT_PROPRIETAIRE));
        JButton commission = UiTheme.boutonAccent("Payer la commission…");
        commission.addActionListener(e -> sortie(TypeMouvement.PAIEMENT_COMMISSION));
        bas.add(mouvements);
        bas.add(versement);
        bas.add(commission);
        add(bas, BorderLayout.SOUTH);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                charger();
            }
        });
    }

    private void charger() {
        List<ResumeCaisse> liste = UiUtil.appeler(this, () -> caisseService.toutes(acteur));
        if (liste == null) {
            return;
        }
        modele.remplacer(liste);
        BigDecimal total = liste.stream().map(ResumeCaisse::getSolde).reduce(MoneyUtil.ZERO, BigDecimal::add);
        BigDecimal dues = liste.stream().map(ResumeCaisse::getCommissionDue).reduce(MoneyUtil.ZERO, BigDecimal::add);
        labelTotal.setText("Argent dans les caisses : " + MoneyUtil.formater(total)
                + "   ·   Commissions dues : " + MoneyUtil.formater(dues));
    }

    private ResumeCaisse selection() {
        int l = table.getSelectedRow();
        if (l < 0) {
            UiUtil.avertir(this, "Sélectionnez une caisse.");
            return null;
        }
        return modele.get(table.convertRowIndexToModel(l));
    }

    private void voirMouvements() {
        ResumeCaisse c = selection();
        if (c == null) {
            return;
        }
        List<MouvementCaisse> m = UiUtil.appeler(this, () -> caisseService.mouvements(acteur, c.getReparateurId()));
        if (m == null) {
            return;
        }
        MouvementsTableModel mm = new MouvementsTableModel();
        mm.remplacer(m);
        JTable t = new JTable(mm);
        UiTheme.styliserTable(t);
        JScrollPane sp = new JScrollPane(t);
        sp.setPreferredSize(new Dimension(760, 300));
        JOptionPane.showMessageDialog(this, sp, "Mouvements — caisse de " + c.getNomReparateur(), JOptionPane.PLAIN_MESSAGE);
    }

    private void sortie(TypeMouvement type) {
        ResumeCaisse c = selection();
        if (c == null) {
            return;
        }
        String aide = type == TypeMouvement.PAIEMENT_COMMISSION
                ? "Commission due : " + MoneyUtil.formater(c.getCommissionDue())
                : "Solde de la caisse : " + MoneyUtil.formater(c.getSolde());
        JTextField montant = new JTextField(type == TypeMouvement.PAIEMENT_COMMISSION
                ? c.getCommissionDue().min(c.getSolde()).max(BigDecimal.ZERO).toPlainString()
                : c.getSolde().toPlainString(), 10);
        JTextField libelle = new JTextField(22);
        JPanel p = new JPanel(new GridLayout(0, 1, 4, 4));
        p.add(new JLabel(type.getLibelle() + " — " + c.getNomReparateur()));
        p.add(UiTheme.aide(aide));
        p.add(new JLabel("Montant (DH)"));
        p.add(montant);
        p.add(new JLabel("Libellé (facultatif)"));
        p.add(libelle);
        if (JOptionPane.showConfirmDialog(this, p, type.getLibelle(), JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return;
        }
        BigDecimal m;
        try {
            m = MoneyUtil.parser(montant.getText());
        } catch (NumberFormatException e) {
            UiUtil.avertir(this, "Montant invalide.");
            return;
        }
        if (UiUtil.executer(this, () -> caisseService.enregistrerSortie(acteur, c.getReparateurId(), type, m,
                libelle.getText()))) {
            UiUtil.succes(this, type.getLibelle() + " enregistré : " + MoneyUtil.formater(m) + ".");
            charger();
        }
    }

    /** Modèle du tableau des caisses. */
    private static class CaissesTableModel extends AbstractTableModel {
        private static final String[] COLONNES = {"Réparateur", "Solde", "Encaissé", "Pièces", "Versé",
            "Comm. acquise", "Comm. payée", "Comm. due", "Comm. à venir"};
        private final List<ResumeCaisse> lignes = new ArrayList<>();

        void remplacer(List<ResumeCaisse> l) {
            lignes.clear();
            lignes.addAll(l);
            fireTableDataChanged();
        }

        ResumeCaisse get(int i) {
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
            ResumeCaisse r = lignes.get(l);
            return switch (c) {
                case 0 -> r.getNomReparateur() + (r.isActif() ? "" : " (désactivé)");
                case 1 -> MoneyUtil.formater(r.getSolde());
                case 2 -> MoneyUtil.formater(r.getTotalEncaisse());
                case 3 -> MoneyUtil.formater(r.getTotalAchatsPieces());
                case 4 -> MoneyUtil.formater(r.getTotalVerse());
                case 5 -> MoneyUtil.formater(r.getCommissionAcquise());
                case 6 -> MoneyUtil.formater(r.getCommissionPayee());
                case 7 -> MoneyUtil.formater(r.getCommissionDue());
                case 8 -> MoneyUtil.formater(r.getCommissionPrevisionnelle());
                default -> "";
            };
        }
    }
}
