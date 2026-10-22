package ma.phonerepair.service.exception;

/** Saisie invalide (champ vide, format incorrect, montant négatif...). */
public class ValidationException extends BusinessException {

    public ValidationException(String message) {
        super(message);
    }
}
