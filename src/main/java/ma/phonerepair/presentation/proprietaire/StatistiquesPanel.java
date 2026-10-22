package ma.phonerepair.presentation.proprietaire;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import ma.phonerepair.model.PerformanceReparateur;
import ma.phonerepair.model.Statistiques;
import ma.phonerepair.model.Utilisateur;
import ma.phonerepair.model.enums.Periode;
import ma.phonerepair.presentation.CarteChiffre;
import ma.phonerepair.presentation.UiTheme;
import ma.phonerepair.presentation.UiUtil;
import ma.phonerepair.service.StatistiqueService;
import ma.phonerepair.util.DateUtil;
import ma.phonerepair.util.MoneyUtil;

/**
 * Résultats jour / semaine / mois / total (EF-21), performance des réparateurs (EF-22)
 * et export CSV (EF-25). Chaque indicateur affiche sa définition.
 */
public class StatistiquesPanel extends JPanel {

    private final StatistiqueService statistiqueService = new StatistiqueService();
    private final Utilisateur acteur;

    private final JComboBox<Periode> champPeriode = new JComboBox<>(Periode.values());
    private final JTextField champDate = new JTextField(DateUtil.formater(LocalDate.now()), 9);
    private final JLabel labelPeriode = UiTheme.aide(" ");
    private final List<CarteChiffre> cartes = new ArrayList<>();
    private final DefaultTableModel modelePerf = new DefaultTableModel(
            new String[] {"Réparateur", "Appareils livrés", "Facturé", "Main d'œuvre", "Commission"}, 0) {
        @Override
        public boolean isCellEditable(int l, int c) {
            return false;
        }
    };
    private Statistiques derniere;

    /** Titre, définition, couleur de chaque carte (même ordre que {@link #valeurs}). */
    private static final Object[][] INDICATEURS = {
        {"Réparations créées", "nouvelles commandes", UiTheme.PRIMAIRE},
        {"Appareils livrés", "rendus au client", UiTheme.PRIMAIRE},
        {"Montant facturé", "pièces + main d'œuvre (livrés)", UiTheme.PRIMAIRE},
        {"Encaissé", "paiements reçus sur la période", UiTheme.SUCCES},
        {"Coût des pièces", "prix d'achat (livrés)", UiTheme.GRIS},
        {"Marge sur pièces", "prix client − coût d'achat", UiTheme.SUCCES},
        {"Main d'œuvre", "total facturé (livrés)", UiTheme.PRIMAIRE},
        {"Commissions", "part des réparateurs", UiTheme.ACCENT},
        {"Part propriétaire MO", "main d'œuvre − commissions", UiTheme.SUCCES},
        {"Marge brute boutique", "marge pièces + part MO (hors charges)", UiTheme.SUCCES},
        {"Reste à encaisser", "aujourd'hui, toutes réparations", UiTheme.DANGER},
        {"Argent en caisses", "aujourd'hui, toutes caisses", UiTheme.PRIMAIRE},
    };

    public StatistiquesPanel(Utilisateur acteur) {
        super(new BorderLayout(0, 12));
        this.acteur = acteur;
        setBackground(UiTheme.FOND);
        setBorder(BorderFactory.createEmptyBorder(20, 28, 20, 28));

        JPanel haut = new JPanel(new BorderLayout(0, 8));
        haut.setOpaque(false);
        haut.add(UiTheme.titre("Statistiques"), BorderLayout.NORTH);
        JPanel filtres = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        filtres.setOpaque(false);
        filtres.add(new JLabel("Période :"));
        filtres.add(champPeriode);
        filtres.add(new JLabel("Date (jj/mm/aaaa) :"));
        filtres.add(champDate);
        JButton calculer = UiTheme.boutonPrincipal("Afficher");
        calculer.addActionListener(e -> calculer());
        champPeriode.addActionListener(e -> calculer());
        JButton exporter = UiTheme.boutonSecondaire("Exporter en CSV…");
        exporter.addActionListener(e -> exporter());
        filtres.add(calculer);
        filtres.add(exporter);
        JPanel ligneFiltres = new JPanel(new GridLayout(2, 1, 0, 2));
        ligneFiltres.setOpaque(false);
        ligneFiltres.add(filtres);
        ligneFiltres.add(labelPeriode);
        haut.add(ligneFiltres, BorderLayout.CENTER);

        JPanel grille = new JPanel(new GridLayout(3, 4, 10, 10));
        grille.setOpaque(false);
        for (Object[] ind : INDICATEURS) {
            CarteChiffre c = new CarteChiffre((String) ind[0], (String) ind[1], (java.awt.Color) ind[2]);
            cartes.add(c);
            grille.add(c);
        }
        haut.add(grille, BorderLayout.SOUTH);
        add(haut, BorderLayout.NORTH);

        JPanel bas = new JPanel(new BorderLayout(0, 4));
        bas.setOpaque(false);
        bas.add(UiTheme.sousTitre("Performance des réparateurs (appareils livrés sur la période)"), BorderLayout.NORTH);
        JTable table = new JTable(modelePerf);
        UiTheme.styliserTable(table);
        for (int c = 1; c < 5; c++) {
            table.getColumnModel().getColumn(c).setCellRenderer(UiTheme.rendreMontant());
        }
        bas.add(new JScrollPane(table), BorderLayout.CENTER);
        bas.add(UiTheme.aide("« Marge brute » ne déduit ni loyer, ni électricité, ni salaires : ce n'est pas un bénéfice."),
                BorderLayout.SOUTH);
        add(bas, BorderLayout.CENTER);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                calculer();
            }
        });
    }

    private void calculer() {
        LocalDate jour;
        try {
            jour = DateUtil.parser(champDate.getText());
        } catch (DateTimeParseException e) {
            UiUtil.avertir(this, "Date invalide (format jj/mm/aaaa).");
            return;
        }
        Periode p = (Periode) champPeriode.getSelectedItem();
        Statistiques s = UiUtil.appeler(this, () -> statistiqueService.calculer(acteur, p, jour));
        if (s == null) {
            return;
        }
        derniere = s;
        labelPeriode.setText(s.getDebut() == null ? "depuis le début"
                : "du " + DateUtil.formater(s.getDebut().toLocalDate()) + " au "
                        + DateUtil.formater(s.getFin().toLocalDate().minusDays(1)));
        String[] v = valeurs(s);
        for (int i = 0; i < v.length; i++) {
            cartes.get(i).setValeur(v[i]);
        }
        modelePerf.setRowCount(0);
        for (PerformanceReparateur pr : s.getPerformances()) {
            modelePerf.addRow(new Object[] {pr.getNomReparateur(), pr.getAppareilsLivres(),
                MoneyUtil.formater(pr.getMontantFacture()), MoneyUtil.formater(pr.getMainOeuvre()),
                MoneyUtil.formater(pr.getCommission())});
        }
    }

    private static String[] valeurs(Statistiques s) {
        BigDecimal[] montants = {s.getMontantFacture(), s.getTotalEncaisse(), s.getCoutAchatPieces(), s.getMargePieces(),
            s.getMainOeuvre(), s.getCommissions(), s.getPartProprietaireMO(), s.getMargeBruteBoutique(),
            s.getResteAEncaisser(), s.getSoldeCaisses()};
        String[] v = new String[2 + montants.length];
        v[0] = String.valueOf(s.getNombreReparations());
        v[1] = String.valueOf(s.getNombreAppareilsLivres());
        for (int i = 0; i < montants.length; i++) {
            v[2 + i] = MoneyUtil.formater(montants[i]);
        }
        return v;
    }

    /** Export CSV (séparateur « ; », lisible par Excel en français). */
    private void exporter() {
        if (derniere == null) {
            return;
        }
        JFileChooser choix = new JFileChooser();
        choix.setSelectedFile(new java.io.File("statistiques-" + derniere.getPeriode().name().toLowerCase() + ".csv"));
        if (choix.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        List<String> lignes = new ArrayList<>();
        lignes.add("Indicateur;Valeur;Définition");
        lignes.add("Période;" + derniere.getPeriode().getLibelle() + ";" + labelPeriode.getText());
        String[] v = valeurs(derniere);
        for (int i = 0; i < INDICATEURS.length; i++) {
            lignes.add(INDICATEURS[i][0] + ";" + v[i].replace(" ", " ") + ";" + INDICATEURS[i][1]);
        }
        lignes.add("");
        lignes.add("Réparateur;Appareils livrés;Facturé;Main d'œuvre;Commission");
        for (PerformanceReparateur p : derniere.getPerformances()) {
            lignes.add(p.getNomReparateur() + ";" + p.getAppareilsLivres() + ";" + p.getMontantFacture() + ";"
                    + p.getMainOeuvre() + ";" + p.getCommission());
        }
        try {
            // BOM UTF-8 pour qu'Excel affiche correctement les accents
            Files.write(choix.getSelectedFile().toPath(), ("﻿" + String.join("\r\n", lignes)).getBytes(StandardCharsets.UTF_8));
            UiUtil.succes(this, "Statistiques exportées dans " + choix.getSelectedFile().getName());
        } catch (IOException e) {
            UiUtil.avertir(this, "Impossible d'écrire le fichier : " + e.getMessage());
        }
    }
}
