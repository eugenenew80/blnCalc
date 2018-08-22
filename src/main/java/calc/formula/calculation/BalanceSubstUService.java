package calc.formula.calculation;

import calc.entity.calc.*;
import calc.entity.calc.enums.BatchStatusEnum;
import calc.entity.calc.enums.ParamTypeEnum;
import calc.entity.calc.enums.PeriodTypeEnum;
import calc.formula.CalcContext;
import calc.formula.CalcResult;
import calc.formula.expression.DoubleExpression;
import calc.formula.expression.impl.PeriodTimeValueExpression;
import calc.formula.service.CalcService;
import calc.formula.service.PeriodTimeValueService;
import calc.repo.calc.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class BalanceSubstUService {
    private static final Logger logger = LoggerFactory.getLogger(BalanceSubstUService.class);
    private final CalcService calcService;
    private final BalanceSubstResultHeaderRepo balanceSubstResultHeaderRepo;
    private final BalanceSubstResultULineRepo balanceSubstResultULineRepo;
    private final ParameterRepo parameterRepo;
    private final PeriodTimeValueService periodTimeValueService;

    public void calc(BalanceSubstResultHeader header)  {
        try {
            logger.info("Uavg for header " + header.getId() + " started");
            updateStatus(header, BatchStatusEnum.P);
            deleteLines(header);
            header = balanceSubstResultHeaderRepo.findOne(header.getId());

            CalcContext context = CalcContext.builder()
                .startDate(header.getStartDate())
                .endDate(header.getEndDate())
                .orgId(header.getOrganization().getId())
                .energyObjectType("SUBSTATION")
                .energyObjectId(header.getSubstation().getId())
                .docCode("ACT")
                .docId(header.getId())
                .trace(new HashMap<>())
                .values(new HashMap<>())
                .build();

            Parameter parU = parameterRepo.findByCode("U");

            List<BalanceSubstResultULine> resultLines = new ArrayList<>();
            List<BalanceSubstULine> uLines = header.getHeader().getULines();
            for (BalanceSubstULine uLine : uLines) {
                BalanceSubstResultULine resultLine = calcLine(uLine.getMeteringPoint(), parU, context);
                resultLine.setHeader(header);
                resultLine.setMeteringPoint(uLine.getMeteringPoint());
                resultLine.setTiNum(uLine.getTiNum());
                resultLine.setTiName(uLine.getTiName());
                resultLines.add(resultLine);
            }

            balanceSubstResultULineRepo.save(resultLines);
            updateStatus(header, BatchStatusEnum.C);
            logger.info("Uavg for header " + header.getId() + " completed");
        }

        catch (Exception e) {
            updateStatus(header, BatchStatusEnum.E);
            logger.error("Uavg for header " + header.getId() + " terminated with exception");
            logger.error(e.toString() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteLines(BalanceSubstResultHeader header) {
        List<BalanceSubstResultULine> lines = balanceSubstResultULineRepo.findAllByHeaderId(header.getId());
        for (int i=0; i<lines.size(); i++)
            balanceSubstResultULineRepo.delete(lines.get(i));
        balanceSubstResultULineRepo.flush();

    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateStatus(BalanceSubstResultHeader header, BatchStatusEnum status) {
        header.setStatus(status);
        balanceSubstResultHeaderRepo.save(header);
    }

    private BalanceSubstResultULine calcLine(MeteringPoint meteringPoint, Parameter parameter, CalcContext context) {
        BalanceSubstResultULine line = new BalanceSubstResultULine();
        try {
            DoubleExpression expression = PeriodTimeValueExpression.builder()
                .meteringPointCode(meteringPoint.getCode())
                .parameterCode(parameter.getCode())
                .rate(1d)
                .startHour((byte) 0)
                .endHour((byte) 23)
                .periodType(PeriodTypeEnum.H)
                .context(context)
                .service(periodTimeValueService)
                .build();

            Double[] values = expression.doubleValues();
            Double sum = 0d;
            Double count = 0d;
            for (Double d : values) {
                if (d != null) {
                    sum+=d;
                    count++;
                }
            }
            if (count != 0)
                line.setVal(sum / count);
            else
                line.setVal(0d);
        }
        catch (Exception e) {
            logger.error("ERROR: " + e.toString() + ", " + e.getMessage());
        }
        return line;
    }
}
