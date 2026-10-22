package ma.phonerepair.service.exception;

/**
 * Une règle de gestion empêche l'opération. Le message est destiné à l'utilisateur
 * et doit être compréhensible (ex. « Transition de statut non autorisée : Prêt → Reçu »).
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
