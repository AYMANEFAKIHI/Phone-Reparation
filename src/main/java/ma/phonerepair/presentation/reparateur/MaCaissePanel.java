package ma.phonerepair.presentation.reparateur;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.math.BigDecimal;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import ma.phonerepair.model.MouvementCaisse;
import ma.phonerepair.model.ResumeCaisse;
import ma.phonerepair.model.Utilisateur;
import ma.phonerepair.model.enums.TypeMouvement;
import ma.phonerepair.presentation.CarteChiffre;
import ma.phonerepair.presentation.MouvementsTableModel;
import ma.phonerepair.presentation.UiTheme;
import ma.phonerepair.presentation.UiUtil;
import ma.phonerepair.service.CaisseService;
import ma.phonerepair.util.MoneyUtil;

/** Caisse du réparateur connecté et sa commission (EF-16). */
public class MaCaissePanel extends JPanel {

    private final CaisseService caisseService = new CaisseService();
    private final Utilisateur acteur;

    private final CarteChiffre carteSolde = new CarteChiffre("Solde", "argent en main", UiTheme.PRIMAIRE);
    private final CarteChiffre carteEncaisse = new CarteChiffre("Encaissé", "paiements clients", UiTheme.SUCCES);
    private final CarteChiffre carteAcquise = new CarteChiffre("Comm. acquise", "appareils livrés", UiTheme.ACCENT);
    private final CarteChiffre carteDue = new CarteChiffre("Comm. due", "acquise − payée", UiTheme.ACCENT);
    private final CarteChiffre cartePrevision = new CarteChiffre("Comm. à venir", "pas encore livrés", UiTheme.GRIS);
    private final MouvementsTableModel modele = new MouvementsTableModel();

    public MaCaissePanel(Utilisateur acteur) {
        super(new BorderLayout(0, 12));
        this.acteur = acteur;
        setBackground(UiTheme.FOND);
        setBorder(BorderFactory.createEmptyBorder(20, 28, 20, 28));

        JPanel haut = new JPanel(new BorderLayout(0, 10));
        haut.setOpaque(false);
        haut.add(UiTheme.titre("Ma caisse"), BorderLayout.NORTH);
        JPanel cartes = new JPanel(new GridLayout(1, 0, 10, 0));
        cartes.setOpaque(false);
        for (CarteChiffre c : new CarteChiffre[] {carteSolde, carteEncaisse, carteAcquise, carteDue, cartePrevision}) {
            cartes.add(c);
        }
        haut.add(cartes, BorderLayout.CENTER);
        add(haut, BorderLayout.NORTH);

        JTable table = new JTable(modele);
        UiTheme.styliserTable(table);
        table.getColumnModel().getColumn(3).setCellRenderer(UiTheme.rendreMontant());
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bas = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        bas.setOpaque(false);
        JButton achat = UiTheme.boutonSecondaire("Achat de pièce payé avec la caisse…");
        achat.addActionListener(e -> achatPieces());
        bas.add(achat);
        bas.add(UiTheme.aide("Les versements au propriétaire et le paiement de la commission sont saisis par le propriétaire."));
        add(bas, BorderLayout.SOUTH);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                charger();
            }
        });
    }

    private void charger() {
        ResumeCaisse c = UiUtil.appeler(this, () -> caisseService.maCaisse(acteur));
        List<MouvementCaisse> m = UiUtil.appeler(this,
                () -> caisseService.mouvements(acteur, acteur.getProfilReparateur().getId()));
        if (c == null || m == null) {
            return;
        }
        carteSolde.setValeur(MoneyUtil.formater(c.getSolde()));
        carteEncaisse.setValeur(MoneyUtil.formater(c.getTotalEncaisse()));
        carteAcquise.setValeur(MoneyUtil.formater(c.getCommissionAcquise()));
        carteDue.setValeur(MoneyUtil.formater(c.getCommissionDue()));
        cartePrevision.setValeur(MoneyUtil.formater(c.getCommissionPrevisionnelle()));
        modele.remplacer(m);
    }

    private void achatPieces() {
        JTextField montant = new JTextField(10);
        JTextField libelle = new JTextField(24);
        JPanel p = new JPanel(new GridLayout(0, 1, 4, 4));
        p.add(new JLabel("Pièce achetée (ex. « Écran iPhone 13 — REP-2026-000001 »)"));
        p.add(libelle);
        p.add(new JLabel("Montant payé (DH)"));
        p.add(montant);
        if (JOptionPane.showConfirmDialog(this, p, "Achat de pièce", JOptionPane.OK_CANCEL_OPTION,
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
        if (UiUtil.executer(this, () -> caisseService.enregistrerSortie(acteur, acteur.getProfilReparateur().getId(),
                TypeMouvement.ACHAT_PIECES, m, libelle.getText()))) {
            charger();
        }
    }
}
