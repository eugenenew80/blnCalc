package calc.formula.exception;

public class ErrorFormulaException extends CalcServiceException {
    public ErrorFormulaException(String message) {
        super("ERROR_FORMULA", message);
    }
}
