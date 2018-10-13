package calc.formula.calculation;

import calc.entity.calc.*;
import calc.entity.calc.bs.BalanceSubstResultHeader;
import calc.entity.calc.bs.u.BalanceSubstResultULine;
import calc.entity.calc.bs.u.BalanceSubstULine;
import calc.entity.calc.enums.PeriodTypeEnum;
import calc.formula.CalcContext;
import calc.formula.expression.DoubleExpression;
import calc.formula.expression.impl.PeriodTimeValueExpression;
import calc.formula.service.MessageService;
import calc.formula.service.ParamService;
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
    private final ParamService paramService;
    private final PeriodTimeValueService periodTimeValueService;
    private final MessageService messageService;
    private final BalanceSubstResultULineRepo balanceSubstResultULineRepo;
    private static final String docCode = "U_AVG";

    public boolean calc(BalanceSubstResultHeader header)  {
        try {
            logger.info("U avg for balance with headerId " + header.getId() + " started");

            CalcContext context = CalcContext.builder()
                .docCode(docCode)
                .docId(header.getId())
                .headerId(header.getId())
                .periodType(header.getPeriodType())
                .startDate(header.getStartDate())
                .endDate(header.getEndDate())
                .orgId(header.getOrganization().getId())
                .energyObjectType("SUBSTATION")
                .energyObjectId(header.getSubstation().getId())
                .trace(new HashMap<>())
                .values(new HashMap<>())
                .build();

            Parameter parU = paramService.getValues().get("U");

            List<BalanceSubstResultULine> resultLines = new ArrayList<>();
            List<BalanceSubstULine> uLines = header.getHeader().getULines();
            for (BalanceSubstULine uLine : uLines) {
                Double val = getVal(uLine.getMeteringPoint(), parU, context);
                BalanceSubstResultULine resultLine = new BalanceSubstResultULine();
                resultLine.setHeader(header);
                resultLine.setMeteringPoint(uLine.getMeteringPoint());
                resultLine.setTiNum(uLine.getTiNum());
                resultLine.setTiName(uLine.getTiName());
                resultLine.setVal(val);
                resultLines.add(resultLine);
            }

            deleteLines(header);
            saveLines(resultLines);

            logger.info("U avg for balance with headerId " + header.getId() + " completed");
            return true;
        }

        catch (Exception e) {
            messageService.addMessage(header, null,  docCode,"RUNTIME_EXCEPTION");
            logger.error("U avg for balance with headerId " + header.getId() + " terminated with exception: " +  e.toString() + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void saveLines(List<BalanceSubstResultULine> resultLines) {
        balanceSubstResultULineRepo.save(resultLines);
        balanceSubstResultULineRepo.flush();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void deleteLines(BalanceSubstResultHeader header) {
        List<BalanceSubstResultULine> lines = balanceSubstResultULineRepo.findAllByHeaderId(header.getId());
        for (int i=0; i<lines.size(); i++)
            balanceSubstResultULineRepo.delete(lines.get(i));
        balanceSubstResultULineRepo.flush();
    }

    private Double getVal(MeteringPoint meteringPoint, Parameter parameter, CalcContext context) {
        DoubleExpression expression = PeriodTimeValueExpression.builder()
            .meteringPointCode(meteringPoint.getCode())
            .parameterCode(parameter.getCode())
            .rate(1d)
            .startHour((byte) 0)
            .endHour((byte) 23)
            .periodType(context.getPeriodType())
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
            return  sum / count;
        else
            return 0d;
    }
}
