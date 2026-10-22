package ma.phonerepair.service;

import java.util.List;
import ma.phonerepair.dao.ClientDAO;
import ma.phonerepair.dao.TransactionManager;
import ma.phonerepair.model.Client;
import ma.phonerepair.model.Utilisateur;
import ma.phonerepair.service.exception.ValidationException;
import ma.phonerepair.util.ValidationUtil;

/** Gestion des clients (EF-07, RG-08, RG-09). Les clients n'ont pas de compte. */
public class ClientService {

    private final ClientDAO clientDAO = new ClientDAO();

    /**
     * Recherche par début de téléphone (« 0612 ») ou par morceau de nom (« amrani »).
     * Minimum 2 caractères pour éviter de charger toute la table.
     */
    public List<Client> rechercher(Utilisateur acteur, String texte) {
        Autorisation.exigerConnecte(acteur);
        if (texte == null || texte.trim().length() < 2) {
            throw new ValidationException("Saisissez au moins 2 caractères (nom ou téléphone).");
        }
        String t = texte.trim();
        String recherche = t.matches("[0-9+ .\\-]+") ? ValidationUtil.normaliserTelephone(t) : t;
        return TransactionManager.lecture(conn -> clientDAO.rechercher(conn, recherche));
    }

    /** Clients ayant exactement ce numéro : à proposer avant de créer un doublon (RG-09). */
    public List<Client> trouverParTelephone(Utilisateur acteur, String telephone) {
        Autorisation.exigerConnecte(acteur);
        String tel = ValidationUtil.normaliserTelephone(telephone);
        return TransactionManager.lecture(conn -> clientDAO.trouverParTelephone(conn, tel));
    }

    public Client creer(Utilisateur acteur, String nomComplet, String telephone) {
        Autorisation.exigerConnecte(acteur);
        BoutiqueService.exigerTexte(nomComplet, "Le nom complet du client", 100);
        if (nomComplet.trim().length() < 3) {
            throw new ValidationException("Le nom complet du client doit contenir au moins 3 caractères.");
        }
        if (!ValidationUtil.estTelephoneValide(telephone)) {
            throw new ValidationException("Téléphone du client invalide (ex. 0612345678).");
        }
        Client c = new Client();
        c.setNomComplet(nomComplet.trim());
        c.setTelephone(ValidationUtil.normaliserTelephone(telephone));
        int id = TransactionManager.enTransaction(conn -> clientDAO.inserer(conn, c));
        c.setId(id);
        return c;
    }
}
