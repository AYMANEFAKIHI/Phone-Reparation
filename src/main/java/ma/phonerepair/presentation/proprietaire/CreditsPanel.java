package ma.phonerepair.presentation.proprietaire;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import ma.phonerepair.model.Credit;
import ma.phonerepair.model.RemboursementCredit;
import ma.phonerepair.model.Utilisateur;
import ma.phonerepair.model.enums.SensCredit;
import ma.phonerepair.presentation.UiTheme;
import ma.phonerepair.presentation.UiUtil;
import ma.phonerepair.service.CreditService;
import ma.phonerepair.util.DateUtil;
import ma.phonerepair.util.MoneyUtil;

/**
 * Crédits entre boutiques (EF-19, EF-20, scénario S10). Séparés des paiements clients (RG-44).
 */
public class CreditsPanel extends JPanel {

    private final CreditService creditService = new CreditService();
    private final Utilisateur acteur;
    private final CreditsTableModel modele = new CreditsTableModel();
    private final JTable table = new JTable(modele);
    private final JLabel labelTotaux = UiTheme.sousTitre(" ");

    public CreditsPanel(Utilisateur acteur) {
        super(new BorderLayout(0, 12));
        this.acteur = acteur;
        setBackground(UiTheme.FOND);
        setBorder(BorderFactory.createEmptyBorder(20, 28, 20, 28));

        JPanel haut = new JPanel(new GridLayout(0, 1, 0, 4));
        haut.setOpaque(false);
        haut.add(UiTheme.titre("Crédits entre boutiques"));
        haut.add(UiTheme.aide("« Accordé » : nous avons prêté, l'autre boutique nous doit. « Reçu » : nous avons emprunté, nous devons."));
        haut.add(labelTotaux);
        add(haut, BorderLayout.NORTH);

        UiTheme.styliserTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        for (int c : new int[] {4, 5, 6}) {
            table.getColumnModel().getColumn(c).setCellRenderer(UiTheme.rendreMontant());
        }
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bas = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        bas.setOpaque(false);
        JButton nouveau = UiTheme.boutonPrincipal("Nouveau crédit…");
        nouveau.addActionListener(e -> nouveauCredit());
        JButton rembourser = UiTheme.boutonAccent("Enregistrer un remboursement…");
        rembourser.addActionListener(e -> rembourser());
        JButton historique = UiTheme.boutonSecondaire("Historique des remboursements");
        historique.addActionListener(e -> historique());
        bas.add(nouveau);
        bas.add(rembourser);
        bas.add(historique);
        add(bas, BorderLayout.SOUTH);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                charger();
            }
        });
    }

    private void charger() {
        List<Credit> l = UiUtil.appeler(this, () -> creditService.lister(acteur));
        if (l == null) {
            return;
        }
        modele.remplacer(l);
        labelTotaux.setText("Les autres boutiques nous doivent : " + MoneyUtil.formater(CreditService.totalRestant(l, SensCredit.ACCORDE))
                + "   ·   Nous devons : " + MoneyUtil.formater(CreditService.totalRestant(l, SensCredit.RECU)));
    }

    private Credit selection() {
        int l = table.getSelectedRow();
        if (l < 0) {
            UiUtil.avertir(this, "Sélectionnez un crédit.");
            return null;
        }
        return modele.get(table.convertRowIndexToModel(l));
    }

    private void nouveauCredit() {
        JComboBox<SensCredit> sens = new JComboBox<>(SensCredit.values());
        JTextField boutique = new JTextField(22);
        JTextField proprietaire = new JTextField(22);
        JTextField patente = new JTextField(22);
        JTextField tel = new JTextField(22);
        JTextField montant = new JTextField(10);
        JTextField date = new JTextField(DateUtil.formater(LocalDate.now()), 10);
        JTextField description = new JTextField(22);
        JPanel p = new JPanel(new GridLayout(0, 2, 6, 6));
        Object[][] lignes = {{"Sens *", sens}, {"Boutique *", boutique}, {"Propriétaire", proprietaire},
            {"N° patente / magasin *", patente}, {"Téléphone", tel}, {"Montant (DH) *", montant},
            {"Date (jj/mm/aaaa) *", date}, {"Description", description}};
        for (Object[] l : lignes) {
            p.add(new JLabel((String) l[0]));
            p.add((java.awt.Component) l[1]);
        }
        if (JOptionPane.showConfirmDialog(this, p, "Nouveau crédit", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return;
        }
        Credit c = new Credit();
        c.setSens((SensCredit) sens.getSelectedItem());
        c.setNomBoutiqueTiers(boutique.getText());
        c.setProprietaireTiers(proprietaire.getText());
        c.setPatenteTiers(patente.getText());
        c.setTelephoneTiers(tel.getText());
        c.setDescription(description.getText());
        try {
            c.setMontant(MoneyUtil.parser(montant.getText()));
            c.setDateCredit(DateUtil.parser(date.getText()));
        } catch (NumberFormatException | DateTimeParseException e) {
            UiUtil.avertir(this, "Montant ou date invalide (ex. 1000 et 14/10/2026).");
            return;
        }
        if (UiUtil.executer(this, () -> creditService.creer(acteur, c))) {
            charger();
        }
    }

    private void rembourser() {
        Credit c = selection();
        if (c == null) {
            return;
        }
        BigDecimal reste = CreditService.resteDu(c);
        JTextField montant = new JTextField(reste.toPlainString(), 10);
        JTextField date = new JTextField(DateUtil.formater(LocalDate.now()), 10);
        JTextField note = new JTextField(22);
        JPanel p = new JPanel(new GridLayout(0, 1, 4, 4));
        p.add(new JLabel(c.getNomBoutiqueTiers() + " — reste dû : " + MoneyUtil.formater(reste)));
        p.add(new JLabel("Montant remboursé (DH)"));
        p.add(montant);
        p.add(new JLabel("Date (jj/mm/aaaa)"));
        p.add(date);
        p.add(new JLabel("Note"));
        p.add(note);
        if (JOptionPane.showConfirmDialog(this, p, "Remboursement", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return;
        }
        BigDecimal m;
        LocalDate d;
        try {
            m = MoneyUtil.parser(montant.getText());
            d = DateUtil.parser(date.getText());
        } catch (NumberFormatException | DateTimeParseException e) {
            UiUtil.avertir(this, "Montant ou date invalide.");
            return;
        }
        Credit apres = UiUtil.appeler(this, () -> creditService.rembourser(acteur, c.getId(), m, d, note.getText()));
        if (apres != null) {
            UiUtil.succes(this, "Remboursement enregistré. Reste dû : " + MoneyUtil.formater(CreditService.resteDu(apres))
                    + " (" + CreditService.statut(apres).getLibelle() + ").");
            charger();
        }
    }

    private void historique() {
        Credit c = selection();
        if (c == null) {
            return;
        }
        DefaultTableModel m = new DefaultTableModel(new String[] {"Date", "Montant", "Note"}, 0);
        for (RemboursementCredit r : c.getRemboursements()) {
            m.addRow(new Object[] {DateUtil.formater(r.getDateRemboursement()), MoneyUtil.formater(r.getMontant()),
                r.getNote() == null ? "" : r.getNote()});
        }
        JTable t = new JTable(m);
        UiTheme.styliserTable(t);
        JScrollPane sp = new JScrollPane(t);
        sp.setPreferredSize(new Dimension(520, 200));
        JOptionPane.showMessageDialog(this, sp, "Remboursements — " + c.getNomBoutiqueTiers(), JOptionPane.PLAIN_MESSAGE);
    }

    /** Modèle du tableau des crédits. */
    private static class CreditsTableModel extends AbstractTableModel {
        private static final String[] COLONNES = {"Date", "Sens", "Boutique", "N° patente", "Montant", "Remboursé",
            "Reste dû", "Statut"};
        private final List<Credit> lignes = new ArrayList<>();

        void remplacer(List<Credit> l) {
            lignes.clear();
            lignes.addAll(l);
            fireTableDataChanged();
        }

        Credit get(int i) {
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
            Credit x = lignes.get(l);
            return switch (c) {
                case 0 -> DateUtil.formater(x.getDateCredit());
                case 1 -> x.getSens() == SensCredit.ACCORDE ? "Accordé" : "Reçu";
                case 2 -> x.getNomBoutiqueTiers() + (x.getProprietaireTiers() == null ? "" : " (" + x.getProprietaireTiers() + ")");
                case 3 -> x.getPatenteTiers();
                case 4 -> MoneyUtil.formater(x.getMontant());
                case 5 -> MoneyUtil.formater(CreditService.totalRembourse(x));
                case 6 -> MoneyUtil.formater(CreditService.resteDu(x));
                case 7 -> CreditService.statut(x).getLibelle();
                default -> "";
            };
        }
    }
}
