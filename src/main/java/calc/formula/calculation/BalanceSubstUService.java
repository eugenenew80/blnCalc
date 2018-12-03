package calc.formula.calculation;

import calc.entity.calc.*;
import calc.entity.calc.bs.BalanceSubstResultHeader;
import calc.entity.calc.bs.u.BalanceSubstResultULine;
import calc.entity.calc.bs.u.BalanceSubstULine;
import calc.entity.calc.enums.LangEnum;
import calc.formula.CalcContext;
import calc.formula.CalcResult;
import calc.formula.ContextTypeEnum;
import calc.formula.exception.CalcServiceException;
import calc.formula.expression.impl.PeriodTimeValueExpression;
import calc.formula.service.CalcService;
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

import static calc.util.Util.buildMsgParams;
import static calc.util.Util.round;
import static java.util.Optional.*;

@SuppressWarnings("Duplicates")
@Service
@RequiredArgsConstructor
public class BalanceSubstUService {
    private static final Logger logger = LoggerFactory.getLogger(BalanceSubstUService.class);
    private final ParamService paramService;
    private final PeriodTimeValueService periodTimeValueService;
    private final MessageService messageService;
    private final CalcService calcService;

    private final BalanceSubstResultULineRepo balanceSubstResultULineRepo;
    private static final String docCode = "U_AVG";

    public boolean calc(BalanceSubstResultHeader header)  {
        try {
            logger.info("U avg for balance with headerId " + header.getId() + " started");

            CalcContext context = CalcContext.builder()
                .lang(LangEnum.RU)
                .header(header)
                .defContextType(ContextTypeEnum.DEFAULT)
                .build();

            Parameter parU = paramService.getValues().get("U");

            List<BalanceSubstResultULine> resultLines = new ArrayList<>();
            List<BalanceSubstULine> uLines = header.getHeader().getULines();
            for (BalanceSubstULine uLine : uLines) {
                MeteringPoint meteringPoint = uLine.getMeteringPoint();
                Map<String, String> msgParams = buildMsgParams(meteringPoint);

                Double val = null;
                try {
                    val = getVal(meteringPoint, parU, context);
                }
                catch (CalcServiceException e) {
                    msgParams.putIfAbsent("err", e.getMessage());
                    messageService.addMessage(header, uLine.getId(), docCode, e.getErrCode(), msgParams);
                }

                BalanceSubstResultULine resultLine = new BalanceSubstResultULine();
                resultLine.setHeader(header);
                resultLine.setMeteringPoint(meteringPoint);
                resultLine.setTiNum(uLine.getTiNum());
                resultLine.setTiName(ofNullable(uLine.getTiName()).orElse("-"));
                resultLine.setVal(val);
                resultLines.add(resultLine);
            }

            deleteLines(header);
            saveLines(resultLines);

            logger.info("U avg for balance with headerId " + header.getId() + " completed");
            return true;
        }

        catch (Exception e) {
            messageService.addMessage(header, null,  docCode,"RUNTIME_EXCEPTION", e.getClass().getCanonicalName());
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

    private Double getVal(MeteringPoint meteringPoint, Parameter param, CalcContext context) throws Exception {
        if (meteringPoint == null || param == null)
            return null;

        Double val = PeriodTimeValueExpression.builder()
            .meteringPointCode(meteringPoint.getCode())
            .parameterCode(param.getCode())
            .context(context)
            .service(periodTimeValueService)
            .build()
            .doubleValue();

        if (ofNullable(val).orElse(0d) == 0d) {
            CalcResult result = calcService.calcValue(meteringPoint, param, context);
            val = result != null ? result.getDoubleValue() : null;
        }

        val = round(val, param);
        return ofNullable(val).orElse(0d);
    }
}
