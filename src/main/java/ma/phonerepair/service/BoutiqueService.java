package ma.phonerepair.service;

import java.util.Optional;
import ma.phonerepair.dao.BoutiqueDAO;
import ma.phonerepair.dao.TransactionManager;
import ma.phonerepair.model.Boutique;
import ma.phonerepair.model.Utilisateur;
import ma.phonerepair.service.exception.ValidationException;
import ma.phonerepair.util.ValidationUtil;

/** Configuration de la boutique (EF-03, RG-06, RG-07). */
public class BoutiqueService {

    private final BoutiqueDAO boutiqueDAO = new BoutiqueDAO();

    /** Lisible par tout le monde (nom affiché dans l'en-tête). */
    public Optional<Boutique> obtenir() {
        return TransactionManager.lecture(boutiqueDAO::trouver);
    }

    public void enregistrer(Utilisateur acteur, Boutique saisie) {
        Autorisation.exigerProprietaire(acteur);
        Boutique b = valider(saisie);
        TransactionManager.enTransaction(conn -> {
            if (boutiqueDAO.trouver(conn).isPresent()) {
                boutiqueDAO.modifier(conn, b);
            } else {
                boutiqueDAO.inserer(conn, b);
            }
            return null;
        });
    }

    private Boutique valider(Boutique saisie) {
        if (saisie == null) {
            throw new ValidationException("Aucune information de boutique.");
        }
        exigerTexte(saisie.getNom(), "Le nom de la boutique", 100);
        exigerTexte(saisie.getAdresse(), "L'adresse", 255);
        exigerTexte(saisie.getNumeroPatente(), "Le N° de patente", 30);
        if (!ValidationUtil.estTelephoneValide(saisie.getTelephone())) {
            throw new ValidationException("Téléphone invalide (ex. 0612345678 ou +212612345678).");
        }
        Boutique b = new Boutique();
        b.setNom(saisie.getNom().trim());
        b.setAdresse(saisie.getAdresse().trim());
        b.setNumeroPatente(saisie.getNumeroPatente().trim());
        b.setTelephone(ValidationUtil.normaliserTelephone(saisie.getTelephone()));
        return b;
    }

    static void exigerTexte(String valeur, String libelle, int max) {
        if (ValidationUtil.estVide(valeur)) {
            throw new ValidationException(libelle + " est obligatoire.");
        }
        if (ValidationUtil.depasse(valeur.trim(), max)) {
            throw new ValidationException(libelle + " ne doit pas dépasser " + max + " caractères.");
        }
    }
}
