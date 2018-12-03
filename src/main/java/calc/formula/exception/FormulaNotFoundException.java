package calc.formula.exception;

public class FormulaNotFoundException extends CalcServiceException {
    public FormulaNotFoundException(String message) {
        super("FORMULA_NOT_FOUND", message);
    }
}
