package calc.formula.calculation;

import calc.entity.calc.*;
import calc.entity.calc.bs.BalanceSubstResultHeader;
import calc.entity.calc.bs.u.*;
import calc.entity.calc.enums.*;
import calc.formula.*;
import calc.formula.exception.*;
import calc.formula.service.*;
import calc.repo.calc.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import static calc.util.Util.*;
import static java.util.Optional.*;

@Service
@RequiredArgsConstructor
public class BalanceSubstUService {
    private static final Logger logger = LoggerFactory.getLogger(BalanceSubstUService.class);
    private static final String docCode = "U_AVG";
    private final ParamService paramService;
    private final MessageService messageService;
    private final CalcService calcService;
    private final BalanceSubstResultULineRepo balanceSubstResultULineRepo;

    public boolean calc(BalanceSubstResultHeader header)  {
        try {
            logger.info(docCode + " for headerId " + header.getId() + " started");
            CalcContext context = CalcContext.builder()
                .lang(LangEnum.RU)
                .header(header)
                .defContextType(ContextTypeEnum.DEFAULT)
                .build();

            Parameter parU = paramService.getParam("U");

            List<BalanceSubstResultULine> results = new ArrayList<>();
            List<BalanceSubstULine> uLines = header.getHeader().getULines();
            for (BalanceSubstULine uLine : uLines) {
                MeteringPoint meteringPoint = uLine.getMeteringPoint();
                Map<String, String> msgParams = buildMsgParams(meteringPoint);

                Double val = null;
                try {
                    val = getMrVal(meteringPoint, parU, context);
                }
                catch (CalcServiceException e) {
                    msgParams.putIfAbsent("err", e.getMessage());
                    messageService.addMessage(header, uLine.getId(), docCode, e.getErrCode(), msgParams);
                }

                BalanceSubstResultULine result = new BalanceSubstResultULine();
                result.setHeader(header);
                result.setMeteringPoint(meteringPoint);
                result.setTiNum(uLine.getTiNum());
                result.setTiName(ofNullable(uLine.getTiName()).orElse("-"));
                result.setVal(val);
                results.add(result);
            }

            deleteLines(header);
            saveLines(results);

            logger.info(docCode + " for headerId " + header.getId() + " completed");
            return true;
        }

        catch (Exception e) {
            e.printStackTrace();
            logger.error(docCode + " for headerId " + header.getId() + " terminated with exception: " +  e.toString() + ": " + e.getMessage());

            messageService.addMessage(header, null,  docCode,"RUNTIME_EXCEPTION", buildMsgParams(e));
            return false;
        }
    }

    private Double getMrVal(MeteringPoint meteringPoint, Parameter param, CalcContext context) {
        if (meteringPoint == null || param == null)
            return null;

        CalcProperty property = CalcProperty.builder()
            .processOrder(ProcessOrderEnum.READ_CALC)
            .contextType(context.getDefContextType())
            .build();

        CalcResult calc = calcService.calcValue(meteringPoint, param, context, property);
        Double val = calc != null ? calc.getDoubleValue() : null;
        val = round(val, param);

        return ofNullable(val).orElse(0d);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 60)
    void saveLines(List<BalanceSubstResultULine> resultLines) {
        balanceSubstResultULineRepo.save(resultLines);
        balanceSubstResultULineRepo.flush();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 60)
    void deleteLines(BalanceSubstResultHeader header) {
        List<BalanceSubstResultULine> lines = balanceSubstResultULineRepo.findAllByHeaderId(header.getId());
        balanceSubstResultULineRepo.delete(lines);
        balanceSubstResultULineRepo.flush();
    }
}
