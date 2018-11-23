package calc.formula.calculation;

import calc.entity.calc.*;
import calc.entity.calc.bs.pe.BalanceSubstPeLine;
import calc.entity.calc.bs.BalanceSubstResultHeader;
import calc.entity.calc.bs.pe.ReactorValue;
import calc.entity.calc.enums.LangEnum;
import calc.formula.CalcContext;
import calc.formula.ContextType;
import calc.formula.expression.impl.*;
import calc.formula.service.*;
import calc.repo.calc.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

import static calc.util.Util.round;

@Service
@RequiredArgsConstructor
public class BalanceSubstReactorService {
    private static final Logger logger = LoggerFactory.getLogger(BalanceSubstReactorService.class);
    private static final String docCode = "LOSSES";
    private final WorkingHoursService workingHoursService;
    private final ReactorService reactorService;
    private final BalanceSubstResultUService resultUService;
    private final MessageService messageService;
    private final ParamService paramService;
    private final ReactorValueRepo reactorValueRepo;

    public boolean calc(BalanceSubstResultHeader header)  {
        try {
            logger.info("Reactor losses for balance with headerId " + header.getId() + " started");

            CalcContext context = CalcContext.builder()
                .lang(LangEnum.RU)
                .docCode(docCode)
                .headerId(header.getId())
                .periodType(header.getPeriodType())
                .startDate(header.getStartDate())
                .endDate(header.getEndDate())
                .orgId(header.getOrganization().getId())
                .energyObjectType("SUBSTATION")
                .energyObjectId(header.getSubstation().getId())
                .defContextType(ContextType.MR)
                .values(new HashMap<>())
                .build();

            List<ReactorValue> reactorLines = calcLines(header, context);
            deleteLines(header);
            saveLines(reactorLines);

            logger.info("Reactor losses for balance with headerId " + header.getId() + " completed");
            return true;
        }

        catch (Exception e) {
            messageService.addMessage(header, null,  docCode,"RUNTIME_EXCEPTION", e.getClass().getCanonicalName());
            logger.error("Reactor losses for balance with headerId " + header.getId() + " terminated with exception: " + e.toString() + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private List<ReactorValue> calcLines(BalanceSubstResultHeader header, CalcContext context) {
        Parameter paramWL = paramService.getValues().get("WL");
        Unit unitWL = paramWL.getUnit();

        List<ReactorValue> reactorLines = new ArrayList<>();
        for (BalanceSubstPeLine peLine : header.getHeader().getPeLines()) {
            Reactor reactor = peLine.getReactor();
            if (reactor == null)
                continue;

            String info = "";
            if (reactor.getTranslates().containsKey(context.getLang()))
                info = reactor.getTranslates()
                    .get(context.getLang())
                    .getName();

            MeteringPoint inputMp = reactor.getInputMp();
            if (inputMp == null) {
                messageService.addMessage(header, peLine.getId(), docCode, "PE_INPUT_NOT_FOUND", info);
                continue;
            }

            Double uNom    = getReactorAttr(reactor, "unom",     context);
            Double deltaPr = getReactorAttr(reactor, "delta_pr", context);

            Double hours = WorkingHoursExpression.builder()
                .objectType("re")
                .objectId(reactor.getId())
                .context(context)
                .service(workingHoursService)
                .build()
                .doubleValue();

            hours = round(hours, 1);

            Double uAvg = UavgExpression.builder()
                .meteringPointCode(inputMp.getCode())
                .def(inputMp.getVoltageClass()!=null ? inputMp.getVoltageClass().getValue() / 1000d : 0d)
                .context(context)
                .service(resultUService)
                .build()
                .doubleValue();

            Parameter parU = paramService.getValues().get("U");
            uAvg = round(uAvg, parU);

            if (uNom == 0) {
                messageService.addMessage(header, peLine.getId(), docCode, "PE_UNOM_NOT_FOUND", info);
                continue;
            }

            Double val = round(deltaPr * hours * Math.pow(uAvg / uNom, 2), paramWL);

            ReactorValue reactorLine = new ReactorValue();
            reactorLine.setHeader(header);
            reactorLine.setReactor(reactor);
            reactorLine.setDeltaPr(deltaPr);
            reactorLine.setOperatingTime(hours);
            reactorLine.setUavg(uAvg);
            reactorLine.setUnom(uNom);
            reactorLine.setUnit(unitWL);
            reactorLine.setVal(val);
            reactorLine.setInputMp(inputMp);
            reactorLine.setIsBalance(peLine.getIsBalance());
            reactorLine.setMeteringPointOut(peLine.getMeteringPointOut());
            reactorLine.setIsBalance(peLine.getIsBalance());
            reactorLines.add(reactorLine);
        }
        return reactorLines;
    }

    private Double getReactorAttr(Reactor reactor, String attr, CalcContext context) {
        return ReactorExpression.builder()
            .id(reactor.getId())
            .attr(attr)
            .def(0d)
            .context(context)
            .service(reactorService)
            .build()
            .doubleValue();
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void saveLines(List<ReactorValue> reactorLines) {
        reactorValueRepo.save(reactorLines);
        reactorValueRepo.flush();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void deleteLines(BalanceSubstResultHeader header) {
        List<ReactorValue> reactorValues = reactorValueRepo.findAllByHeaderId(header.getId());
        for (int i=0; i<reactorValues.size(); i++)
            reactorValueRepo.delete(reactorValues.get(i));
        reactorValueRepo.flush();
    }
}
