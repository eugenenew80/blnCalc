package calc.formula.exception;

import java.util.List;

public class CycleDetectionException extends Exception {
    private String code;
    private List<String> codes;

    public CycleDetectionException(String message) {
        super(message);
    }

    public CycleDetectionException(String message, String code) {
        super(message);
        this.code = code;
    }

    public CycleDetectionException(String message, List<String> codes) {
        super(message);
        this.codes = codes;
    }

    public String getCode() {
        return code;
    }

    public List<String> getCodes() {
        return codes;
    }
}
