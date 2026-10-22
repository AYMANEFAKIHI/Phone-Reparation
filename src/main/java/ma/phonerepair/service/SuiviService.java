package ma.phonerepair.service;

import java.util.List;
import java.util.logging.Logger;
import ma.phonerepair.dao.AppareilDAO;
import ma.phonerepair.dao.ReparationDAO;
import ma.phonerepair.dao.TransactionManager;
import ma.phonerepair.model.Appareil;
import ma.phonerepair.model.Reparation;
import ma.phonerepair.model.SuiviClient;
import ma.phonerepair.service.exception.BusinessException;
import ma.phonerepair.service.exception.ValidationException;
import ma.phonerepair.util.CodeGenerator;

/**
 * Suivi d'une réparation par le client, sans compte (EF-23, RG-45, Q4).
 * Le client fournit le code ET les 4 derniers chiffres de son téléphone.
 */
public class SuiviService {

    private static final Logger LOG = Logger.getLogger(SuiviService.class.getName());
    static final String MSG_INTROUVABLE = "Aucune réparation trouvée pour ce code et ce numéro.";

    private final ReparationDAO reparationDAO = new ReparationDAO();
    private final AppareilDAO appareilDAO = new AppareilDAO();
    private final PaiementService paiementService = new PaiementService();

    public SuiviClient suivre(String code, String quatreDerniersChiffres) {
        String c = CodeGenerator.normaliser(code);
        if (!CodeGenerator.estFormatValide(c)) {
            throw new ValidationException("Code de suivi invalide. Format attendu : REP-2026-000123.");
        }
        String chiffres = quatreDerniersChiffres == null ? "" : quatreDerniersChiffres.trim();
        if (!chiffres.matches("\\d{4}")) {
            throw new ValidationException("Saisissez les 4 derniers chiffres de votre numéro de téléphone.");
        }
        return TransactionManager.lecture(conn -> {
            Reparation r = reparationDAO.trouverParCode(conn, c).orElse(null);
            // Même message si le code n'existe pas ou si le téléphone ne correspond pas :
            // on ne révèle pas l'existence d'un code.
            if (r == null || !r.getClient().getTelephone().endsWith(chiffres)) {
                LOG.info("Suivi refusé pour le code " + c);
                throw new BusinessException(MSG_INTROUVABLE);
            }
            List<Appareil> appareils = appareilDAO.listerParReparation(conn, r.getId());
            SuiviClient s = new SuiviClient();
            s.setCodeSuivi(r.getCodeSuivi());
            s.setNomClient(r.getClient().getNomComplet());
            s.setDateDepot(r.getDateCreation());
            for (Appareil a : appareils) {
                s.getAppareils().add(new SuiviClient.LigneSuivi(a.getDesignation(), a.getProblemeSignale(),
                        a.getStatut(), a.getDateMajStatut()));
            }
            s.setStatutGlobal(ReparationService.calculerStatutGlobal(
                    appareils.stream().map(Appareil::getStatut).toList()));
            s.setSituation(paiementService.situation(conn, r.getId()));
            return s;
        });
    }
}
