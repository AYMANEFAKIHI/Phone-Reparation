package ma.phonerepair.presentation.reparateur;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.math.BigDecimal;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import ma.phonerepair.model.DossierPaiement;
import ma.phonerepair.model.Paiement;
import ma.phonerepair.model.SituationFinanciere;
import ma.phonerepair.model.Utilisateur;
import ma.phonerepair.model.enums.TypePaiement;
import ma.phonerepair.presentation.UiTheme;
import ma.phonerepair.presentation.UiUtil;
import ma.phonerepair.service.PaiementService;
import ma.phonerepair.util.DateUtil;
import ma.phonerepair.util.MoneyUtil;

/**
 * Encaissement d'un paiement client (EF-13, EF-14, scénarios S5 et S8).
 * Le paiement entre dans la caisse du réparateur connecté (RG-36).
 */
public class PaiementPanel extends JPanel {

    private final PaiementService paiementService = new PaiementService();
    private final Utilisateur acteur;
    private DossierPaiement dossier;

    private final JTextField champCode = new JTextField("REP-", 16);
    private final JLabel labelClient = UiTheme.sousTitre(" ");
    private final JLabel labelSituation = new JLabel(" ");
    private final JTextField champMontant = new JTextField(10);
    private final JComboBox<TypePaiement> champMode = new JComboBox<>(TypePaiement.values());
    private final JTextField champNote = new JTextField(22);
    private final JButton boutonEncaisser = UiTheme.boutonPrincipal("Encaisser");
    private final DefaultTableModel modele = new DefaultTableModel(
            new String[] {"Date", "Montant", "Mode", "Encaissé par", "Note"}, 0) {
        @Override
        public boolean isCellEditable(int l, int c) {
            return false;
        }
    };

    public PaiementPanel(Utilisateur acteur, String codeInitial) {
        super(new BorderLayout(0, 12));
        this.acteur = acteur;
        setBackground(UiTheme.FOND);
        setBorder(BorderFactory.createEmptyBorder(20, 28, 20, 28));

        JPanel haut = new JPanel(new GridLayout(0, 1, 0, 6));
        haut.setOpaque(false);
        haut.add(UiTheme.titre("Encaissement"));
        JPanel recherche = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        recherche.setOpaque(false);
        recherche.add(new JLabel("Code de suivi :"));
        recherche.add(champCode);
        JButton charger = UiTheme.boutonSecondaire("Afficher");
        charger.addActionListener(e -> charger());
        champCode.addActionListener(e -> charger());
        recherche.add(charger);
        haut.add(recherche);
        haut.add(labelClient);
        labelSituation.setFont(UiTheme.SOUS_TITRE);
        haut.add(labelSituation);
        add(haut, BorderLayout.NORTH);

        JTable table = new JTable(modele);
        UiTheme.styliserTable(table);
        table.getColumnModel().getColumn(1).setCellRenderer(UiTheme.rendreMontant());
        JPanel centre = new JPanel(new BorderLayout(0, 4));
        centre.setOpaque(false);
        centre.add(new JLabel("Paiements déjà reçus"), BorderLayout.NORTH);
        centre.add(new JScrollPane(table), BorderLayout.CENTER);
        add(centre, BorderLayout.CENTER);
        add(construireFormulaire(), BorderLayout.SOUTH);

        activerFormulaire(false);
        if (codeInitial != null) {
            champCode.setText(codeInitial);
            charger();
        }
    }

    private JPanel construireFormulaire() {
        JPanel p = new JPanel(new GridLayout(2, 1, 0, 6));
        p.setBackground(UiTheme.BLANC);
        p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(UiTheme.BORDURE),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        JPanel ligne1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        JPanel ligne2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        ligne1.setOpaque(false);
        ligne2.setOpaque(false);
        p.add(ligne1);
        p.add(ligne2);
        ligne1.add(new JLabel("Montant (DH)"));
        ligne1.add(champMontant);
        JButton toutLeReste = UiTheme.boutonSecondaire("Tout le reste");
        toutLeReste.addActionListener(e -> {
            if (dossier != null) {
                champMontant.setText(dossier.getSituation().getReste().toPlainString());
            }
        });
        ligne1.add(toutLeReste);
        ligne1.add(new JLabel("Mode"));
        ligne1.add(champMode);
        ligne2.add(new JLabel("Note"));
        ligne2.add(champNote);
        boutonEncaisser.addActionListener(e -> encaisser());
        ligne2.add(boutonEncaisser);
        return p;
    }

    private void activerFormulaire(boolean actif) {
        champMontant.setEnabled(actif);
        champMode.setEnabled(actif);
        champNote.setEnabled(actif);
        boutonEncaisser.setEnabled(actif);
    }

    private void charger() {
        DossierPaiement d = UiUtil.appeler(this, () -> paiementService.chargerDossier(acteur, champCode.getText()));
        if (d == null) {
            return;
        }
        dossier = d;
        champCode.setText(d.getCodeSuivi());
        labelClient.setText(d.getCodeSuivi() + " — " + d.getNomClient() + " — " + d.getTelephoneClient());
        SituationFinanciere s = d.getSituation();
        labelSituation.setText("Total " + MoneyUtil.formater(s.getTotal()) + "   ·   Payé " + MoneyUtil.formater(s.getPaye())
                + "   ·   Reste " + MoneyUtil.formater(s.getReste()) + "   (" + s.getStatut().getLibelle() + ")");
        labelSituation.setForeground(s.getReste().signum() > 0 ? UiTheme.ACCENT : UiTheme.SUCCES);
        modele.setRowCount(0);
        for (Paiement p : d.getPaiements()) {
            modele.addRow(new Object[] {DateUtil.formater(p.getDatePaiement()), MoneyUtil.formater(p.getMontant()),
                    p.getTypePaiement().getLibelle(), p.getNomUtilisateur(), p.getNote() == null ? "" : p.getNote()});
        }
        activerFormulaire(s.getReste().signum() > 0);
        champMontant.setText("");
        champNote.setText("");
    }

    private void encaisser() {
        if (dossier == null) {
            return;
        }
        BigDecimal montant;
        try {
            montant = MoneyUtil.parser(champMontant.getText());
        } catch (NumberFormatException e) {
            UiUtil.avertir(this, "Montant invalide (ex. 200 ou 199,50).");
            return;
        }
        TypePaiement mode = (TypePaiement) champMode.getSelectedItem();
        if (!UiUtil.confirmer(this, "Encaisser " + MoneyUtil.formater(montant) + " (" + mode.getLibelle() + ") pour "
                + dossier.getCodeSuivi() + " ?")) {
            return;
        }
        SituationFinanciere s = UiUtil.appeler(this, () -> paiementService.enregistrer(acteur,
                dossier.getReparationId(), montant, mode, champNote.getText()));
        if (s != null) {
            UiUtil.succes(this, "Paiement enregistré dans votre caisse.\n\nPayé : " + MoneyUtil.formater(s.getPaye())
                    + "\nReste : " + MoneyUtil.formater(s.getReste()) + "\nStatut : " + s.getStatut().getLibelle());
            charger();
        }
    }
}
