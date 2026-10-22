package ma.phonerepair.service;

import java.time.Year;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import ma.phonerepair.dao.AppareilDAO;
import ma.phonerepair.dao.DataAccessException;
import ma.phonerepair.dao.HistoriqueStatutDAO;
import ma.phonerepair.dao.InterventionDAO;
import ma.phonerepair.dao.ReparationDAO;
import ma.phonerepair.dao.TransactionManager;
import ma.phonerepair.model.Appareil;
import ma.phonerepair.model.Client;
import ma.phonerepair.model.HistoriqueStatut;
import ma.phonerepair.model.Intervention;
import ma.phonerepair.model.Reparation;
import ma.phonerepair.model.ResumeReparation;
import ma.phonerepair.model.Utilisateur;
import ma.phonerepair.model.enums.StatutGlobal;
import ma.phonerepair.model.enums.StatutReparation;
import ma.phonerepair.service.exception.AccesRefuseException;
import ma.phonerepair.service.exception.BusinessException;
import ma.phonerepair.service.exception.ValidationException;
import ma.phonerepair.util.CodeGenerator;
import ma.phonerepair.util.ValidationUtil;

/**
 * Réparations (commandes) : création avec plusieurs appareils et un seul code de suivi
 * (RG-10 à RG-14), consultation, statut global (RG-19).
 */
public class ReparationService {

    static final int MAX_APPAREILS = 10;
    private static final int TENTATIVES_CODE = 3;

    private final ReparationDAO reparationDAO = new ReparationDAO();
    private final AppareilDAO appareilDAO = new AppareilDAO();
    private final InterventionDAO interventionDAO = new InterventionDAO();
    private final HistoriqueStatutDAO historiqueDAO = new HistoriqueStatutDAO();

    /**
     * Statut global d'une réparation, calculé à partir des statuts de ses appareils (RG-19).
     * Ordre d'évaluation : première règle vraie.
     */
    public static StatutGlobal calculerStatutGlobal(Collection<StatutReparation> statuts) {
        if (statuts == null || statuts.isEmpty()) {
            return StatutGlobal.RECEIVED;
        }
        boolean tousAnnules = statuts.stream().allMatch(s -> s == StatutReparation.CANCELLED);
        if (tousAnnules) {
            return StatutGlobal.CANCELLED;
        }
        if (statuts.stream().allMatch(StatutReparation::estFinal)) {
            return StatutGlobal.DELIVERED;
        }
        boolean tousPretsOuFinis = statuts.stream()
                .allMatch(s -> s == StatutReparation.READY || s.estFinal());
        if (tousPretsOuFinis) {
            return StatutGlobal.READY;
        }
        if (statuts.stream().allMatch(s -> s == StatutReparation.RECEIVED)) {
            return StatutGlobal.RECEIVED;
        }
        return StatutGlobal.IN_PROGRESS;
    }

    /**
     * Crée la réparation, ses appareils (statut RECEIVED) et leur historique en une transaction,
     * et génère le code de suivi. Le responsable est le réparateur connecté.
     */
    public Reparation creer(Utilisateur acteur, Client client, List<Appareil> appareils, String remarque) {
        Autorisation.exigerProfilReparateur(acteur);
        if (client == null || client.getId() <= 0) {
            throw new ValidationException("Sélectionnez ou créez d'abord le client.");
        }
        if (appareils == null || appareils.isEmpty()) {
            throw new ValidationException("Ajoutez au moins un appareil à la réparation.");
        }
        if (appareils.size() > MAX_APPAREILS) {
            throw new ValidationException("Maximum " + MAX_APPAREILS + " appareils par réparation.");
        }
        appareils.forEach(ReparationService::validerAppareil);
        if (ValidationUtil.depasse(remarque, 500)) {
            throw new ValidationException("La remarque ne doit pas dépasser 500 caractères.");
        }

        Reparation r = new Reparation();
        r.setClient(client);
        r.setReparateurId(acteur.getProfilReparateur().getId());
        r.setNomReparateur(acteur.getNomComplet());
        r.setCreePar(acteur.getId());
        r.setRemarque(ValidationUtil.estVide(remarque) ? null : remarque.trim());

        // En cas de création simultanée sur deux postes, le code peut être pris entre-temps :
        // la contrainte UNIQUE le refuse, on recommence la transaction avec le numéro suivant (RG-12).
        for (int tentative = 1; ; tentative++) {
            try {
                return TransactionManager.enTransaction(conn -> {
                    int annee = Year.now().getValue();
                    r.setCodeSuivi(CodeGenerator.formater(annee, reparationDAO.prochainNumero(conn, annee)));
                    r.setId(reparationDAO.inserer(conn, r));
                    r.getAppareils().clear();
                    for (Appareil saisie : appareils) {
                        r.getAppareils().add(insererAppareil(conn, r.getId(), saisie, acteur));
                    }
                    return r;
                });
            } catch (DataAccessException e) {
                if (!e.concerne("uq_reparation_code") || tentative >= TENTATIVES_CODE) {
                    throw e;
                }
            }
        }
    }

    /** Ajoute un appareil à une réparation non clôturée (EF-09). Le code de suivi ne change pas. */
    public Appareil ajouterAppareil(Utilisateur acteur, int reparationId, Appareil saisie) {
        Autorisation.exigerConnecte(acteur);
        validerAppareil(saisie);
        return TransactionManager.enTransaction(conn -> {
            Reparation r = reparationDAO.trouverParId(conn, reparationId)
                    .orElseThrow(() -> new BusinessException("Réparation introuvable."));
            verifierAcces(acteur, r);
            List<Appareil> existants = appareilDAO.listerParReparation(conn, reparationId);
            if (existants.size() >= MAX_APPAREILS) {
                throw new BusinessException("Maximum " + MAX_APPAREILS + " appareils par réparation.");
            }
            StatutGlobal global = calculerStatutGlobal(existants.stream().map(Appareil::getStatut).toList());
            if (global == StatutGlobal.DELIVERED || global == StatutGlobal.CANCELLED) {
                throw new BusinessException("Cette réparation est clôturée : créez une nouvelle réparation.");
            }
            return insererAppareil(conn, reparationId, saisie, acteur);
        });
    }

    /** Réparation complète : client, appareils et leurs interventions. */
    public Reparation obtenir(Utilisateur acteur, int reparationId) {
        Autorisation.exigerConnecte(acteur);
        return TransactionManager.lecture(conn -> {
            Reparation r = reparationDAO.trouverParId(conn, reparationId)
                    .orElseThrow(() -> new BusinessException("Réparation introuvable."));
            verifierAcces(acteur, r);
            List<Appareil> appareils = appareilDAO.listerParReparation(conn, reparationId);
            Map<Integer, List<Intervention>> parAppareil = interventionDAO.listerParReparation(conn, reparationId)
                    .stream().collect(Collectors.groupingBy(Intervention::getAppareilId));
            for (Appareil a : appareils) {
                a.setInterventions(parAppareil.getOrDefault(a.getId(), new java.util.ArrayList<>()));
            }
            r.setAppareils(appareils);
            return r;
        });
    }

    /**
     * Liste des réparations visibles par l'acteur (EF-12) : toutes pour le propriétaire,
     * les siennes pour un réparateur.
     *
     * @param texte  filtre sur code / nom / téléphone (facultatif)
     * @param statut filtre sur le statut global (null = tous)
     */
    public List<ResumeReparation> lister(Utilisateur acteur, String texte, StatutGlobal statut) {
        Autorisation.exigerConnecte(acteur);
        Integer filtreReparateur = acteur.estProprietaire() ? null : idProfil(acteur);
        List<ResumeReparation> liste = TransactionManager.lecture(
                conn -> reparationDAO.lister(conn, filtreReparateur, texte));
        for (ResumeReparation r : liste) {
            r.setStatutGlobal(calculerStatutGlobal(r.getStatutsAppareils()));
            r.setSituation(PaiementService.calculerSituation(r.getTotal(), r.getPaye()));
        }
        if (statut != null) {
            liste.removeIf(r -> r.getStatutGlobal() != statut);
        }
        return liste;
    }

    /** Propriétaire : tout. Réparateur : uniquement les réparations dont il est responsable. */
    static void verifierAcces(Utilisateur acteur, Reparation r) {
        if (acteur.estProprietaire()) {
            return;
        }
        if (!acteur.aProfilReparateur() || acteur.getProfilReparateur().getId() != r.getReparateurId()) {
            throw new AccesRefuseException("Cette réparation est suivie par un autre réparateur.");
        }
    }

    private Appareil insererAppareil(java.sql.Connection conn, int reparationId, Appareil saisie,
                                     Utilisateur acteur) throws java.sql.SQLException {
        Appareil a = nettoyer(saisie);
        a.setReparationId(reparationId);
        a.setStatut(StatutReparation.RECEIVED);
        a.setId(appareilDAO.inserer(conn, a));

        HistoriqueStatut h = new HistoriqueStatut();
        h.setAppareilId(a.getId());
        h.setAncienStatut(null);
        h.setNouveauStatut(StatutReparation.RECEIVED);
        h.setUtilisateurId(acteur.getId());
        h.setCommentaire("Dépôt de l'appareil");
        historiqueDAO.inserer(conn, h);
        return a;
    }

    /** Validation d'un appareil — publique pour que l'écran signale l'erreur dès l'ajout à la liste. */
    public static void validerAppareil(Appareil a) {
        if (a == null) {
            throw new ValidationException("Appareil vide.");
        }
        BoutiqueService.exigerTexte(a.getMarque(), "La marque", 50);
        BoutiqueService.exigerTexte(a.getModele(), "Le modèle", 80);
        BoutiqueService.exigerTexte(a.getProblemeSignale(), "La panne déclarée", 500);
        if (!ValidationUtil.estVide(a.getImeiSerie()) && !a.getImeiSerie().trim().matches("[A-Za-z0-9]{5,30}")) {
            throw new ValidationException("IMEI / N° de série invalide : 5 à 30 lettres ou chiffres, sans espace.");
        }
        if (ValidationUtil.depasse(a.getDescription(), 255) || ValidationUtil.depasse(a.getEtatPhysique(), 255)) {
            throw new ValidationException("Description et état physique : 255 caractères maximum.");
        }
    }

    private static Appareil nettoyer(Appareil s) {
        Appareil a = new Appareil();
        a.setMarque(s.getMarque().trim());
        a.setModele(s.getModele().trim());
        a.setProblemeSignale(s.getProblemeSignale().trim());
        a.setImeiSerie(ValidationUtil.estVide(s.getImeiSerie()) ? null : s.getImeiSerie().trim());
        a.setDescription(ValidationUtil.estVide(s.getDescription()) ? null : s.getDescription().trim());
        a.setEtatPhysique(ValidationUtil.estVide(s.getEtatPhysique()) ? null : s.getEtatPhysique().trim());
        return a;
    }

    private static Integer idProfil(Utilisateur acteur) {
        return acteur.aProfilReparateur() ? acteur.getProfilReparateur().getId() : -1;
    }
}
