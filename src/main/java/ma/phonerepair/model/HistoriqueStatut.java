package ma.phonerepair.model;

import java.time.LocalDateTime;

import ma.phonerepair.model.enums.StatutReparation;

/**
 * Trace d'un changement de statut d'un appareil (RG-16).
 */
public class HistoriqueStatut {

    private int id;
    private int appareilId;
    private StatutReparation ancienStatut;
    private StatutReparation nouveauStatut;
    private LocalDateTime dateChangement;
    private int utilisateurId;
    private String nomUtilisateur;
    private String commentaire;

    public HistoriqueStatut() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAppareilId() {
        return appareilId;
    }

    public void setAppareilId(int appareilId) {
        this.appareilId = appareilId;
    }

    public StatutReparation getAncienStatut() {
        return ancienStatut;
    }

    public void setAncienStatut(StatutReparation ancienStatut) {
        this.ancienStatut = ancienStatut;
    }

    public StatutReparation getNouveauStatut() {
        return nouveauStatut;
    }

    public void setNouveauStatut(StatutReparation nouveauStatut) {
        this.nouveauStatut = nouveauStatut;
    }

    public LocalDateTime getDateChangement() {
        return dateChangement;
    }

    public void setDateChangement(LocalDateTime dateChangement) {
        this.dateChangement = dateChangement;
    }

    public int getUtilisateurId() {
        return utilisateurId;
    }

    public void setUtilisateurId(int utilisateurId) {
        this.utilisateurId = utilisateurId;
    }

    public String getNomUtilisateur() {
        return nomUtilisateur;
    }

    public void setNomUtilisateur(String nomUtilisateur) {
        this.nomUtilisateur = nomUtilisateur;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }
}
