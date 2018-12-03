package calc.formula.exception;

public class ValueNotFoundException extends CalcServiceException {
    public ValueNotFoundException(String message) {
        super("VALUE_NOT_FOUND", message);
    }
}
