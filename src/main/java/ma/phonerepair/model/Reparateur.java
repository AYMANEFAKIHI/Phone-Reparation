package ma.phonerepair.model;

import java.math.BigDecimal;

/**
 * Profil réparateur d'un utilisateur : taux de commission actuel (RG-31).
 */
public class Reparateur {

    private int id;
    private int utilisateurId;
    private BigDecimal tauxCommission;

    public Reparateur() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUtilisateurId() {
        return utilisateurId;
    }

    public void setUtilisateurId(int utilisateurId) {
        this.utilisateurId = utilisateurId;
    }

    public BigDecimal getTauxCommission() {
        return tauxCommission;
    }

    public void setTauxCommission(BigDecimal tauxCommission) {
        this.tauxCommission = tauxCommission;
    }
}
