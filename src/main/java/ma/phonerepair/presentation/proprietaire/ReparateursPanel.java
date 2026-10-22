package ma.phonerepair.presentation.proprietaire;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import ma.phonerepair.model.Utilisateur;
import ma.phonerepair.presentation.UiTheme;
import ma.phonerepair.presentation.UiUtil;
import ma.phonerepair.service.ReparateurService;
import ma.phonerepair.util.MoneyUtil;

/** Gestion des réparateurs et de leur commission (EF-04, EF-05, EF-06). */
public class ReparateursPanel extends JPanel {

    private final ReparateurService reparateurService = new ReparateurService();
    private final Utilisateur acteur;
    private final Consumer<Utilisateur> proprietaireModifie;

    private final ComptesTableModel modele = new ComptesTableModel();
    private final JTable table = new JTable(modele);

    public ReparateursPanel(Utilisateur acteur, Consumer<Utilisateur> proprietaireModifie) {
        super(new BorderLayout(0, 12));
        this.acteur = acteur;
        this.proprietaireModifie = proprietaireModifie;
        setBackground(UiTheme.FOND);
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        JPanel entete = new JPanel(new BorderLayout());
        entete.setOpaque(false);
        entete.add(UiTheme.titre("Réparateurs"), BorderLayout.NORTH);
        entete.add(UiTheme.aide("Le taux est la part du réparateur sur la main d'œuvre. "
                + "Un nouveau taux ne s'applique qu'aux nouvelles interventions."), BorderLayout.SOUTH);
        add(entete, BorderLayout.NORTH);

        UiTheme.styliserTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(construireBoutons(), BorderLayout.SOUTH);
        charger();
    }

    private JPanel construireBoutons() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        p.setOpaque(false);

        JButton nouveau = UiTheme.boutonPrincipal("Nouveau réparateur");
        nouveau.addActionListener(e -> nouveauReparateur());
        JButton taux = UiTheme.boutonSecondaire("Modifier le taux");
        taux.addActionListener(e -> modifierTaux());
        JButton actif = UiTheme.boutonSecondaire("Activer / désactiver");
        actif.addActionListener(e -> basculerActif());
        JButton motDePasse = UiTheme.boutonSecondaire("Réinitialiser le mot de passe");
        motDePasse.addActionListener(e -> reinitialiserMotDePasse());
        p.add(nouveau);
        p.add(taux);
        p.add(actif);
        p.add(motDePasse);

        if (!acteur.aProfilReparateur()) {
            JButton jeRepare = UiTheme.boutonAccent("Je répare aussi");
            jeRepare.setToolTipText("Créer votre profil réparateur et votre caisse, sans second compte");
            jeRepare.addActionListener(e -> activerProfilProprietaire());
            p.add(jeRepare);
        }
        return p;
    }

    private void charger() {
        UiUtil.executer(this, () -> modele.remplacer(reparateurService.listerComptes(acteur)));
    }

    private Utilisateur selection() {
        int ligne = table.getSelectedRow();
        if (ligne < 0) {
            UiUtil.avertir(this, "Sélectionnez d'abord un compte dans le tableau.");
            return null;
        }
        return modele.get(table.convertRowIndexToModel(ligne));
    }

    private void nouveauReparateur() {
        if (new ReparateurDialog(SwingUtilities.getWindowAncestor(this), acteur).afficher()) {
            charger();
        }
    }

    private void modifierTaux() {
        Utilisateur cible = selection();
        if (cible == null) {
            return;
        }
        if (!cible.aProfilReparateur()) {
            UiUtil.avertir(this, "Ce compte n'a pas de profil réparateur.");
            return;
        }
        BigDecimal taux = demanderTaux("Nouveau taux de commission pour " + cible.getNomComplet() + " (%) :",
                cible.getProfilReparateur().getTauxCommission());
        if (taux != null && UiUtil.executer(this, () -> reparateurService.modifierTaux(acteur, cible, taux))) {
            charger();
        }
    }

    private void basculerActif() {
        Utilisateur cible = selection();
        if (cible == null) {
            return;
        }
        boolean nouvelEtat = !cible.isActif();
        String question = (nouvelEtat ? "Réactiver" : "Désactiver") + " le compte de " + cible.getNomComplet() + " ?";
        if (UiUtil.confirmer(this, question)
                && UiUtil.executer(this, () -> reparateurService.changerActif(acteur, cible, nouvelEtat))) {
            charger();
        }
    }

    private void reinitialiserMotDePasse() {
        Utilisateur cible = selection();
        if (cible == null) {
            return;
        }
        JPasswordField champ = new JPasswordField(16);
        int choix = JOptionPane.showConfirmDialog(this, champ,
                "Nouveau mot de passe pour " + cible.getUsername(), JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        if (choix == JOptionPane.OK_OPTION && UiUtil.executer(this,
                () -> reparateurService.reinitialiserMotDePasse(acteur, cible, champ.getPassword()))) {
            UiUtil.succes(this, "Mot de passe réinitialisé.");
        }
    }

    private void activerProfilProprietaire() {
        BigDecimal taux = demanderTaux("Votre taux de commission sur vos propres réparations (%) :",
                new BigDecimal("100"));
        if (taux == null) {
            return;
        }
        Utilisateur[] misAJour = new Utilisateur[1];
        if (UiUtil.executer(this, () -> misAJour[0] = reparateurService.activerProfilProprietaire(acteur, taux))) {
            UiUtil.succes(this, "Profil réparateur activé : vous avez maintenant une caisse et les menus réparation.");
            proprietaireModifie.accept(misAJour[0]);
        }
    }

    /** @return le taux saisi, ou null si annulé / invalide */
    private BigDecimal demanderTaux(String message, BigDecimal valeurInitiale) {
        Object saisie = JOptionPane.showInputDialog(this, message, "Taux de commission",
                JOptionPane.PLAIN_MESSAGE, null, null, valeurInitiale.stripTrailingZeros().toPlainString());
        if (saisie == null) {
            return null;
        }
        try {
            return MoneyUtil.parser(saisie.toString().replace("%", ""));
        } catch (NumberFormatException e) {
            UiUtil.avertir(this, "Le taux doit être un nombre (ex. 60).");
            return null;
        }
    }

    /** Modèle du tableau des comptes. */
    private static class ComptesTableModel extends AbstractTableModel {
        private static final String[] COLONNES = {"Nom", "Utilisateur", "Téléphone", "Rôle", "Commission", "Statut"};
        private final List<Utilisateur> lignes = new ArrayList<>();

        void remplacer(List<Utilisateur> nouvelles) {
            lignes.clear();
            lignes.addAll(nouvelles);
            fireTableDataChanged();
        }

        Utilisateur get(int index) {
            return lignes.get(index);
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
        public String getColumnName(int colonne) {
            return COLONNES[colonne];
        }

        @Override
        public Object getValueAt(int ligne, int colonne) {
            Utilisateur u = lignes.get(ligne);
            return switch (colonne) {
                case 0 -> u.getNomComplet();
                case 1 -> u.getUsername();
                case 2 -> u.getTelephone() == null ? "" : u.getTelephone();
                case 3 -> u.getRole().getLibelle() + (u.estProprietaire() && u.aProfilReparateur() ? " + réparateur" : "");
                case 4 -> u.aProfilReparateur()
                        ? u.getProfilReparateur().getTauxCommission().stripTrailingZeros().toPlainString() + " %"
                        : "—";
                case 5 -> u.isActif() ? "Actif" : "Désactivé";
                default -> "";
            };
        }
    }
}
