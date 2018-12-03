package calc.formula.exception;

import lombok.Getter;

public class CalcServiceException extends RuntimeException {
    @Getter
    private String errCode;

    public CalcServiceException(String errCode, String message) {
        super(message);
        this.errCode = errCode;
    }
}
