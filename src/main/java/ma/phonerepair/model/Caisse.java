package ma.phonerepair.model;

import java.time.LocalDateTime;

/**
 * Caisse d'un réparateur. Le solde n'est pas stocké : il est calculé à partir des mouvements (RG-37).
 */
public class Caisse {

    private int id;
    private int reparateurId;
    private LocalDateTime dateCreation;

    public Caisse() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getReparateurId() {
        return reparateurId;
    }

    public void setReparateurId(int reparateurId) {
        this.reparateurId = reparateurId;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }
}
