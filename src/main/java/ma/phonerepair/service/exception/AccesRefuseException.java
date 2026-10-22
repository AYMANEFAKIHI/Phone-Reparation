package ma.phonerepair.service.exception;

/** L'utilisateur connecté n'a pas le droit d'effectuer cette action (RG-05). */
public class AccesRefuseException extends BusinessException {

    public AccesRefuseException(String message) {
        super(message);
    }
}
