package ma.phonerepair.presentation;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.time.LocalDate;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import ma.phonerepair.model.ResumeCaisse;
import ma.phonerepair.model.Statistiques;
import ma.phonerepair.model.Utilisateur;
import ma.phonerepair.model.enums.Periode;
import ma.phonerepair.service.CaisseService;
import ma.phonerepair.service.StatistiqueService;
import ma.phonerepair.util.DateUtil;
import ma.phonerepair.util.MoneyUtil;

/**
 * Page d'accueil : indicateurs du jour pour le propriétaire, caisse et commission pour un réparateur.
 * Rafraîchie à chaque affichage.
 */
public class AccueilPanel extends JPanel {

    private final Utilisateur utilisateur;
    private final CarteChiffre[] cartes;

    public AccueilPanel(Utilisateur utilisateur) {
        super(new BorderLayout(0, 16));
        this.utilisateur = utilisateur;
        setBackground(UiTheme.FOND);
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        JPanel bloc = new JPanel();
        bloc.setOpaque(false);
        bloc.setLayout(new BoxLayout(bloc, BoxLayout.Y_AXIS));
        bloc.add(UiTheme.titre("Bonjour " + utilisateur.getNomComplet()));
        bloc.add(UiTheme.aide(texteRole(utilisateur) + " — " + DateUtil.formater(LocalDate.now())));

        if (utilisateur.estProprietaire()) {
            cartes = new CarteChiffre[] {
                new CarteChiffre("Réparations du jour", "créées aujourd'hui", UiTheme.PRIMAIRE),
                new CarteChiffre("Appareils livrés", "aujourd'hui", UiTheme.PRIMAIRE),
                new CarteChiffre("Encaissé aujourd'hui", "paiements clients", UiTheme.SUCCES),
                new CarteChiffre("Reste à encaisser", "toutes réparations", UiTheme.DANGER)};
        } else {
            cartes = new CarteChiffre[] {
                new CarteChiffre("Solde de ma caisse", "argent en main", UiTheme.PRIMAIRE),
                new CarteChiffre("Commission due", "à recevoir du propriétaire", UiTheme.ACCENT),
                new CarteChiffre("Commission à venir", "appareils pas encore livrés", UiTheme.GRIS)};
        }
        JPanel grille = new JPanel(new GridLayout(1, 0, 10, 0));
        grille.setOpaque(false);
        for (CarteChiffre c : cartes) {
            grille.add(c);
        }
        JPanel haut = new JPanel(new BorderLayout(0, 16));
        haut.setOpaque(false);
        haut.add(bloc, BorderLayout.NORTH);
        haut.add(grille, BorderLayout.CENTER);
        add(haut, BorderLayout.NORTH);

        // Premier affichage (CardLayout n'envoie pas componentShown pour la carte initiale)
        javax.swing.SwingUtilities.invokeLater(this::charger);
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                charger();
            }
        });
    }

    private void charger() {
        if (utilisateur.estProprietaire()) {
            Statistiques s = UiUtil.appeler(this,
                    () -> new StatistiqueService().calculer(utilisateur, Periode.JOUR, LocalDate.now()));
            if (s != null) {
                cartes[0].setValeur(String.valueOf(s.getNombreReparations()));
                cartes[1].setValeur(String.valueOf(s.getNombreAppareilsLivres()));
                cartes[2].setValeur(MoneyUtil.formater(s.getTotalEncaisse()));
                cartes[3].setValeur(MoneyUtil.formater(s.getResteAEncaisser()));
            }
        } else if (utilisateur.aProfilReparateur()) {
            ResumeCaisse c = UiUtil.appeler(this, () -> new CaisseService().maCaisse(utilisateur));
            if (c != null) {
                cartes[0].setValeur(MoneyUtil.formater(c.getSolde()));
                cartes[1].setValeur(MoneyUtil.formater(c.getCommissionDue()));
                cartes[2].setValeur(MoneyUtil.formater(c.getCommissionPrevisionnelle()));
            }
        }
    }

    private static String texteRole(Utilisateur u) {
        if (u.estProprietaire()) {
            return u.aProfilReparateur() ? "Propriétaire et réparateur" : "Propriétaire";
        }
        return "Réparateur";
    }
}
