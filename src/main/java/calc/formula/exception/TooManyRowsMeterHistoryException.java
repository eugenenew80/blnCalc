package calc.formula.exception;

public class TooManyRowsMeterHistoryException extends CalcServiceException {
    public TooManyRowsMeterHistoryException(String message) {
        super("MDFEM_ERROR", message);
    }
}
