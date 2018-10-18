package calc.formula.service;

import calc.entity.calc.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
public class MeteringReading {
    private MeteringPoint meteringPoint;
    private MeteringPoint bypassMeteringPoint;
    private Parameter param;
    private Unit unit;
    private Meter meter;
    private MeterHistory meterHistory;
    private BypassMode bypassMode;
    private UnderCount underCount;
    private LocalDateTime startMeteringDate;
    private LocalDateTime endMeteringDate;
    private Double startVal;
    private Double endVal;
    private Double delta;
    private Double val;
    private Double meterRate;
    private Double underCountVal;
    private Boolean isBypassSection;
}
