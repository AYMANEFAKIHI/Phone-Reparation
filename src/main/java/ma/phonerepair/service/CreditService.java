package ma.phonerepair.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import ma.phonerepair.dao.CreditDAO;
import ma.phonerepair.dao.TransactionManager;
import ma.phonerepair.model.Credit;
import ma.phonerepair.model.RemboursementCredit;
import ma.phonerepair.model.Utilisateur;
import ma.phonerepair.model.enums.SensCredit;
import ma.phonerepair.model.enums.StatutCredit;
import ma.phonerepair.service.exception.BusinessException;
import ma.phonerepair.service.exception.ValidationException;
import ma.phonerepair.util.MoneyUtil;
import ma.phonerepair.util.ValidationUtil;

/**
 * Crédits entre notre boutique et d'autres boutiques (EF-19, EF-20, RG-40 à RG-44).
 * Totalement séparés des paiements clients et des caisses. Réservé au propriétaire.
 */
public class CreditService {

    private final CreditDAO creditDAO = new CreditDAO();

    /** Total déjà remboursé. */
    public static BigDecimal totalRembourse(Credit c) {
        return c.getRemboursements().stream().map(RemboursementCredit::getMontant)
                .reduce(MoneyUtil.ZERO, BigDecimal::add);
    }

    /** Reste dû = montant − Σ remboursements (RG-43). */
    public static BigDecimal resteDu(Credit c) {
        return MoneyUtil.arrondir(c.getMontant()).subtract(totalRembourse(c));
    }

    /** EN_COURS si rien remboursé, SOLDE si reste = 0, sinon PARTIEL (RG-43). */
    public static StatutCredit statut(Credit c) {
        if (resteDu(c).signum() <= 0) {
            return StatutCredit.SOLDE;
        }
        return totalRembourse(c).signum() == 0 ? StatutCredit.EN_COURS : StatutCredit.PARTIEL;
    }

    /** Somme des restes dus pour un sens donné (ex. tout ce que les autres boutiques nous doivent). */
    public static BigDecimal totalRestant(List<Credit> credits, SensCredit sens) {
        return credits.stream().filter(c -> c.getSens() == sens).map(CreditService::resteDu)
                .reduce(MoneyUtil.ZERO, BigDecimal::add);
    }

    public List<Credit> lister(Utilisateur acteur) {
        Autorisation.exigerProprietaire(acteur);
        return TransactionManager.lecture(creditDAO::listerTous);
    }

    public Credit creer(Utilisateur acteur, Credit saisie) {
        Autorisation.exigerProprietaire(acteur);
        Credit c = valider(saisie);
        c.setCreePar(acteur.getId());
        return TransactionManager.enTransaction(conn -> {
            c.setId(creditDAO.inserer(conn, c));
            return c;
        });
    }

    /** Remboursement partiel ou total (EF-20, RG-42). @return le crédit mis à jour */
    public Credit rembourser(Utilisateur acteur, int creditId, BigDecimal montant, LocalDate date, String note) {
        Autorisation.exigerProprietaire(acteur);
        if (!MoneyUtil.estStrictementPositif(montant) || montant.stripTrailingZeros().scale() > MoneyUtil.ECHELLE) {
            throw new ValidationException("Le montant du remboursement doit être supérieur à 0 (2 décimales maximum).");
        }
        if (date == null || date.isAfter(LocalDate.now())) {
            throw new ValidationException("La date du remboursement est obligatoire et ne peut pas être dans le futur.");
        }
        if (ValidationUtil.depasse(note, 255)) {
            throw new ValidationException("La note ne doit pas dépasser 255 caractères.");
        }
        BigDecimal m = MoneyUtil.arrondir(montant);
        return TransactionManager.enTransaction(conn -> {
            creditDAO.verrouiller(conn, creditId);
            Credit c = creditDAO.trouverParId(conn, creditId)
                    .orElseThrow(() -> new BusinessException("Crédit introuvable."));
            if (date.isBefore(c.getDateCredit())) {
                throw new ValidationException("Le remboursement ne peut pas être antérieur à la date du crédit.");
            }
            BigDecimal reste = resteDu(c);
            if (reste.signum() <= 0) {
                throw new BusinessException("Ce crédit est déjà soldé.");
            }
            if (m.compareTo(reste) > 0) {
                throw new ValidationException("Le reste dû est de " + MoneyUtil.formater(reste)
                        + " : le remboursement ne peut pas dépasser ce montant.");
            }
            RemboursementCredit r = new RemboursementCredit();
            r.setCreditId(creditId);
            r.setMontant(m);
            r.setDateRemboursement(date);
            r.setNote(ValidationUtil.estVide(note) ? null : note.trim());
            r.setUtilisateurId(acteur.getId());
            creditDAO.insererRemboursement(conn, r);
            return creditDAO.trouverParId(conn, creditId).orElseThrow();
        });
    }

    private static Credit valider(Credit s) {
        if (s == null || s.getSens() == null) {
            throw new ValidationException("Indiquez le sens du crédit (accordé ou reçu).");
        }
        BoutiqueService.exigerTexte(s.getNomBoutiqueTiers(), "Le nom de l'autre boutique", 100);
        BoutiqueService.exigerTexte(s.getPatenteTiers(), "Le N° de patente / magasin de l'autre boutique", 30);
        if (ValidationUtil.depasse(s.getProprietaireTiers(), 100) || ValidationUtil.depasse(s.getDescription(), 500)) {
            throw new ValidationException("Propriétaire : 100 caractères max, description : 500 caractères max.");
        }
        if (!ValidationUtil.estVide(s.getTelephoneTiers()) && !ValidationUtil.estTelephoneValide(s.getTelephoneTiers())) {
            throw new ValidationException("Téléphone de l'autre boutique invalide (ex. 0612345678).");
        }
        if (!MoneyUtil.estStrictementPositif(s.getMontant()) || s.getMontant().stripTrailingZeros().scale() > 2) {
            throw new ValidationException("Le montant du crédit doit être supérieur à 0 (2 décimales maximum).");
        }
        if (s.getDateCredit() == null || s.getDateCredit().isAfter(LocalDate.now())) {
            throw new ValidationException("La date du crédit est obligatoire et ne peut pas être dans le futur.");
        }
        Credit c = new Credit();
        c.setSens(s.getSens());
        c.setNomBoutiqueTiers(s.getNomBoutiqueTiers().trim());
        c.setPatenteTiers(s.getPatenteTiers().trim());
        c.setProprietaireTiers(ValidationUtil.estVide(s.getProprietaireTiers()) ? null : s.getProprietaireTiers().trim());
        c.setTelephoneTiers(ValidationUtil.estVide(s.getTelephoneTiers()) ? null
                : ValidationUtil.normaliserTelephone(s.getTelephoneTiers()));
        c.setMontant(MoneyUtil.arrondir(s.getMontant()));
        c.setDateCredit(s.getDateCredit());
        c.setDescription(ValidationUtil.estVide(s.getDescription()) ? null : s.getDescription().trim());
        return c;
    }
}
