package calc.formula.exception;

public class CycleDetectionException extends CalcServiceException {
    public CycleDetectionException(String message) {
        super("CYCLED_FORMULA", message);
    }
}
