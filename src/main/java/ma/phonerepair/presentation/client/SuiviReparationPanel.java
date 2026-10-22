package ma.phonerepair.presentation.client;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import ma.phonerepair.model.SuiviClient;
import ma.phonerepair.presentation.UiTheme;
import ma.phonerepair.presentation.UiUtil;
import ma.phonerepair.service.SuiviService;
import ma.phonerepair.util.DateUtil;
import ma.phonerepair.util.MoneyUtil;

/**
 * Suivi d'une réparation par le client, sans connexion (EF-23, scénario S7).
 * Accessible depuis l'écran de connexion. N'affiche que des informations destinées au client.
 */
public class SuiviReparationPanel extends JPanel {

    private final SuiviService suiviService = new SuiviService();

    private final JTextField champCode = new JTextField("REP-", 16);
    private final JTextField champChiffres = new JTextField(5);
    private final JLabel labelTitre = UiTheme.sousTitre(" ");
    private final JLabel labelStatut = new JLabel(" ");
    private final JLabel labelMontants = new JLabel(" ");
    private final DefaultTableModel modele = new DefaultTableModel(
            new String[] {"Appareil", "Panne déclarée", "Statut", "Mis à jour le"}, 0) {
        @Override
        public boolean isCellEditable(int l, int c) {
            return false;
        }
    };

    public SuiviReparationPanel() {
        super(new BorderLayout(0, 12));
        setBackground(UiTheme.FOND);
        setBorder(BorderFactory.createEmptyBorder(20, 28, 20, 28));

        JPanel haut = new JPanel(new BorderLayout(0, 8));
        haut.setOpaque(false);
        haut.add(UiTheme.titre("Suivre ma réparation"), BorderLayout.NORTH);
        JPanel saisie = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        saisie.setOpaque(false);
        saisie.add(new JLabel("Code de suivi :"));
        saisie.add(champCode);
        saisie.add(new JLabel("4 derniers chiffres de votre téléphone :"));
        saisie.add(champChiffres);
        JButton suivre = UiTheme.boutonPrincipal("Suivre");
        suivre.addActionListener(e -> suivre());
        champChiffres.addActionListener(e -> suivre());
        saisie.add(suivre);
        haut.add(saisie, BorderLayout.SOUTH);
        add(haut, BorderLayout.NORTH);

        JPanel resultat = new JPanel(new BorderLayout(0, 8));
        resultat.setOpaque(false);
        JPanel infos = new JPanel(new GridLayout(0, 1, 0, 4));
        infos.setOpaque(false);
        infos.add(labelTitre);
        labelStatut.setFont(UiTheme.POLICE_GRAS);
        infos.add(labelStatut);
        labelMontants.setFont(UiTheme.SOUS_TITRE);
        infos.add(labelMontants);
        resultat.add(infos, BorderLayout.NORTH);
        JTable table = new JTable(modele);
        UiTheme.styliserTable(table);
        table.getColumnModel().getColumn(2).setCellRenderer(UiTheme.rendreStatut());
        resultat.add(new JScrollPane(table), BorderLayout.CENTER);
        add(resultat, BorderLayout.CENTER);
    }

    private void suivre() {
        SuiviClient s = UiUtil.appeler(this, () -> suiviService.suivre(champCode.getText(), champChiffres.getText()));
        modele.setRowCount(0);
        if (s == null) {
            labelTitre.setText(" ");
            labelStatut.setText(" ");
            labelMontants.setText(" ");
            return;
        }
        labelTitre.setText(s.getCodeSuivi() + " — " + s.getNomClient() + " — déposé le " + DateUtil.formater(s.getDateDepot()));
        labelStatut.setText("Avancement : " + s.getStatutGlobal().getLibelle());
        labelStatut.setForeground(UiTheme.couleurStatut(s.getStatutGlobal().name()));
        labelMontants.setText("À payer " + MoneyUtil.formater(s.getSituation().getTotal())
                + "   ·   Déjà payé " + MoneyUtil.formater(s.getSituation().getPaye())
                + "   ·   Reste " + MoneyUtil.formater(s.getSituation().getReste()));
        for (SuiviClient.LigneSuivi l : s.getAppareils()) {
            modele.addRow(new Object[] {l.getDesignation(), l.getProblemeSignale(), l.getStatut(),
                    DateUtil.formater(l.getDateMajStatut())});
        }
    }
}
