package calc.formula;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import static java.util.Optional.ofNullable;

@Getter
@RequiredArgsConstructor
public class ParamValue {
    private final Double ap;
    private final Double am;
    private final Double rp;
    private final Double rm;

    public Double getTotalAE() {
        return ofNullable(ap).orElse(0d) + ofNullable(am).orElse(0d);
    }

    public Double getTotalRE() {
        return ofNullable(rp).orElse(0d) + ofNullable(rm).orElse(0d);
    }

    public Double getTotalE() {
        return Math.pow(getTotalAE(), 2) + Math.pow(getTotalRE(), 2);
    }
}
