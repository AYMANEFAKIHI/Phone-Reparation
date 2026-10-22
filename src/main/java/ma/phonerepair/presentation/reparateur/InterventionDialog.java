package ma.phonerepair.presentation.reparateur;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.math.BigDecimal;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import ma.phonerepair.model.Appareil;
import ma.phonerepair.model.Intervention;
import ma.phonerepair.model.Utilisateur;
import ma.phonerepair.presentation.UiTheme;
import ma.phonerepair.presentation.UiUtil;
import ma.phonerepair.service.AppareilService;
import ma.phonerepair.service.CommissionService;
import ma.phonerepair.util.MoneyUtil;

/**
 * Saisie du diagnostic, des pièces et de la main d'œuvre d'un appareil (EF-10, scénario S4).
 * Les montants sont recalculés en direct ; le taux de commission est figé à la première saisie.
 */
public class InterventionDialog extends JDialog {

    private final AppareilService appareilService = new AppareilService();
    private final Utilisateur acteur;
    private final Appareil appareil;
    private final Intervention existante;

    private final JTextField champDiagnostic = new JTextField(32);
    private final JTextField champPieces = new JTextField(32);
    private final JTextField champCout = new JTextField(10);
    private final JTextField champPrix = new JTextField(10);
    private final JTextField champMainOeuvre = new JTextField(10);
    private final JLabel labelCalcul = new JLabel(" ");
    private boolean enregistre;

    public InterventionDialog(Window parent, Utilisateur acteur, Appareil appareil) {
        super(parent, "Diagnostic et coûts — " + appareil.getDesignation(), ModalityType.APPLICATION_MODAL);
        this.acteur = acteur;
        this.appareil = appareil;
        this.existante = appareil.getInterventions().isEmpty() ? null
                : appareil.getInterventions().get(appareil.getInterventions().size() - 1);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(16, 20, 8, 20));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        c.anchor = GridBagConstraints.WEST;
        int y = 0;
        ligne(form, c, y++, "Panne déclarée", new JLabel(appareil.getProblemeSignale()));
        ligne(form, c, y++, "Diagnostic *", champDiagnostic);
        ligne(form, c, y++, "Pièces nécessaires", champPieces);
        ligne(form, c, y++, "Coût d'achat des pièces (DH)", champCout);
        ligne(form, c, y++, "Prix des pièces client (DH)", champPrix);
        ligne(form, c, y++, "Main d'œuvre (DH)", champMainOeuvre);
        c.gridx = 0;
        c.gridy = y;
        c.gridwidth = 2;
        labelCalcul.setFont(UiTheme.POLICE_GRAS);
        labelCalcul.setForeground(UiTheme.PRIMAIRE);
        form.add(labelCalcul, c);
        c.gridy = y + 1;
        form.add(UiTheme.aide(existante == null
                ? "Le taux de commission actuel du réparateur sera figé à l'enregistrement."
                : "Taux de commission figé pour cet appareil : "
                    + existante.getTauxCommissionApplique().stripTrailingZeros().toPlainString() + " %"), c);

        remplir();
        DocumentListener recalcul = new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { recalculer(); }
            @Override public void removeUpdate(DocumentEvent e) { recalculer(); }
            @Override public void changedUpdate(DocumentEvent e) { recalculer(); }
        };
        for (JTextField f : new JTextField[] {champCout, champPrix, champMainOeuvre}) {
            f.getDocument().addDocumentListener(recalcul);
        }
        recalculer();

        JButton annuler = UiTheme.boutonSecondaire("Annuler");
        annuler.addActionListener(e -> dispose());
        JButton ok = UiTheme.boutonPrincipal("Enregistrer");
        ok.addActionListener(e -> enregistrer());
        JPanel boutons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        boutons.setBorder(BorderFactory.createEmptyBorder(0, 16, 12, 16));
        boutons.add(annuler);
        boutons.add(ok);

        getContentPane().add(form, BorderLayout.CENTER);
        getContentPane().add(boutons, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(ok);
        pack();
        setLocationRelativeTo(parent);
    }

    private static void ligne(JPanel p, GridBagConstraints c, int y, String libelle, JComponent champ) {
        c.gridwidth = 1;
        c.gridx = 0;
        c.gridy = y;
        p.add(new JLabel(libelle), c);
        c.gridx = 1;
        p.add(champ, c);
    }

    private void remplir() {
        if (existante == null) {
            champCout.setText("0");
            champPrix.setText("0");
            champMainOeuvre.setText("0");
            return;
        }
        champDiagnostic.setText(existante.getDiagnostic());
        champPieces.setText(existante.getPiecesDescription() == null ? "" : existante.getPiecesDescription());
        champCout.setText(existante.getCoutAchatPieces().toPlainString());
        champPrix.setText(existante.getPrixPiecesClient().toPlainString());
        champMainOeuvre.setText(existante.getMainOeuvre().toPlainString());
    }

    /** Aperçu en direct. Les calculs sont ceux du service (aucune formule ici). */
    private void recalculer() {
        try {
            BigDecimal cout = MoneyUtil.parser(champCout.getText());
            BigDecimal prix = MoneyUtil.parser(champPrix.getText());
            BigDecimal mo = MoneyUtil.parser(champMainOeuvre.getText());
            Intervention apercu = new Intervention();
            apercu.setPrixPiecesClient(prix);
            apercu.setMainOeuvre(mo);
            String texte = "Total client : " + MoneyUtil.formater(apercu.getMontantClient())
                    + "   ·   Marge pièces : " + MoneyUtil.formater(CommissionService.calculerMargePieces(prix, cout));
            if (existante != null) {
                BigDecimal taux = existante.getTauxCommissionApplique();
                texte += "   ·   Commission : " + MoneyUtil.formater(CommissionService.calculerCommission(mo, taux));
            }
            labelCalcul.setText(texte);
        } catch (NumberFormatException e) {
            labelCalcul.setText("Montant invalide (ex. 300 ou 299,50)");
        }
    }

    private void enregistrer() {
        Intervention saisie = new Intervention();
        saisie.setDiagnostic(champDiagnostic.getText());
        saisie.setPiecesDescription(champPieces.getText());
        try {
            saisie.setCoutAchatPieces(MoneyUtil.parser(champCout.getText()));
            saisie.setPrixPiecesClient(MoneyUtil.parser(champPrix.getText()));
            saisie.setMainOeuvre(MoneyUtil.parser(champMainOeuvre.getText()));
        } catch (NumberFormatException e) {
            UiUtil.avertir(this, "Les montants doivent être des nombres (ex. 300 ou 299,50).");
            return;
        }
        // RG-22 : prix client inférieur au coût → avertissement, pas blocage
        if (saisie.getPrixPiecesClient().compareTo(saisie.getCoutAchatPieces()) < 0
                && !UiUtil.confirmer(this, "Le prix des pièces est inférieur à leur coût d'achat. Continuer ?")) {
            return;
        }
        Intervention resultat = UiUtil.appeler(this,
                () -> appareilService.enregistrerIntervention(acteur, appareil.getId(), saisie));
        if (resultat == null) {
            return;
        }
        BigDecimal taux = resultat.getTauxCommissionApplique();
        UiUtil.succes(this, "Intervention enregistrée.\n\n"
                + "Total client : " + MoneyUtil.formater(resultat.getMontantClient()) + "\n"
                + "Commission réparateur (" + taux.stripTrailingZeros().toPlainString() + " %) : "
                + MoneyUtil.formater(CommissionService.calculerCommission(resultat.getMainOeuvre(), taux)) + "\n"
                + "Part propriétaire (main d'œuvre) : "
                + MoneyUtil.formater(CommissionService.calculerPartProprietaire(resultat.getMainOeuvre(), taux)));
        enregistre = true;
        dispose();
    }

    /** @return true si l'intervention a été enregistrée */
    public boolean afficher() {
        setVisible(true);
        return enregistre;
    }
}
