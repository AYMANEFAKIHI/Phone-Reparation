package ma.phonerepair.presentation;

import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import ma.phonerepair.model.MouvementCaisse;
import ma.phonerepair.util.DateUtil;
import ma.phonerepair.util.MoneyUtil;

/** Relevé d'une caisse (utilisé par « Ma caisse » et par l'écran propriétaire « Caisses »). */
public class MouvementsTableModel extends AbstractTableModel {

    private static final String[] COLONNES = {"Date", "Type", "Libellé", "Montant", "Par"};
    private final List<MouvementCaisse> lignes = new ArrayList<>();

    public void remplacer(List<MouvementCaisse> l) {
        lignes.clear();
        lignes.addAll(l);
        fireTableDataChanged();
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
        MouvementCaisse m = lignes.get(l);
        return switch (c) {
            case 0 -> DateUtil.formater(m.getDateMouvement());
            case 1 -> m.getType().getLibelle();
            case 2 -> m.getLibelle() == null ? "" : m.getLibelle();
            case 3 -> (m.getType().estEntree() ? "+ " : "− ") + MoneyUtil.formater(m.getMontant());
            case 4 -> m.getNomUtilisateur();
            default -> "";
        };
    }
}
