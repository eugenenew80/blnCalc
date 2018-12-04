package calc.formula.exception;

public class TooManyFormulasException extends CalcServiceException {
    public TooManyFormulasException(String message) {
        super("TOO_MANY_FORMULAS", message);
    }
}
