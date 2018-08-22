package calc.formula.calculation;

import calc.entity.calc.*;
import calc.entity.calc.enums.BatchStatusEnum;
import calc.formula.CalcContext;
import calc.formula.expression.DoubleExpression;
import calc.formula.expression.impl.AtTimeValueExpression;
import calc.formula.service.AtTimeValueService;
import calc.formula.service.CalcService;
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
public class BalanceSubstMrService {
    private static final Logger logger = LoggerFactory.getLogger(BalanceSubstMrService.class);
    private final CalcService calcService;
    private final BalanceSubstResultHeaderRepo balanceSubstResultHeaderRepo;
    private final BalanceSubstResultMrLineRepo balanceSubstResultMrLineRepo;
    private final ParameterRepo parameterRepo;
    private final UnitRepo unitRepo;
    private final AtTimeValueService atTimeValueService;
    private final BypassModeRepo bypassModeRepo;
    private final UndercountRepo undercountRepo;
    private final MeterHistoryRepo meterHistoryRepo;

    public void calc(BalanceSubstResultHeader header) {
        try {
            logger.info("Metering reading for header " + header.getId() + " started");
            updateStatus(header, BatchStatusEnum.P);
            deleteLines(header);
            header = balanceSubstResultHeaderRepo.findOne(header.getId());

            Parameter parAp = parameterRepo.findByCode("A+");
            Parameter parAm = parameterRepo.findByCode("A-");
            Parameter parRp = parameterRepo.findByCode("R+");
            Parameter parRm = parameterRepo.findByCode("R-");
            Unit aUnitCode = unitRepo.findByCode("kW.h");

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

            List<BalanceSubstResultMrLine> resultLines = new ArrayList<>();
            List<BalanceSubstMrLine> mrLines = header.getHeader().getMrLines();
            for (BalanceSubstMrLine mrLine : mrLines) {

                List<MeterHistory> meterHistory = meterHistoryRepo.findAllByMeteringPointIdAndDate(
                    mrLine.getMeteringPoint().getId(),
                    context.getStartDate().atStartOfDay()
                );

                List<BypassMode> bypassModes = bypassModeRepo.findAllByMeteringPointIdAndDate(
                    mrLine.getMeteringPoint().getId(),
                    context.getStartDate().atStartOfDay()
                );

                List<Undercount> undercounts = undercountRepo.findAllByMeteringPointIdAndDate(
                    mrLine.getMeteringPoint().getId(),
                    context.getStartDate().atStartOfDay()
                );

                List<Parameter> parameters = new ArrayList<>();
                if (meterHistory!=null && meterHistory.size()>0) {
                    if (meterHistory.get(0).getMeter().getIsAm()) parameters.add(parAm);
                    if (meterHistory.get(0).getMeter().getIsAp()) parameters.add(parAp);
                    if (meterHistory.get(0).getMeter().getIsRm()) parameters.add(parRm);
                    if (meterHistory.get(0).getMeter().getIsRp()) parameters.add(parRp);
                }

                for (Parameter param : parameters) {
                    BalanceSubstResultMrLine line = calcLine(mrLine, param, context);

                    line.setHeader(header);
                    line.setMeteringPoint(mrLine.getMeteringPoint());
                    line.setParam(param);
                    line.setUnit(aUnitCode);
                    line.setStartMeteringDate(context.getStartDate().atStartOfDay());
                    line.setEndMeteringDate(context.getEndDate().atStartOfDay().plusDays(1));

                    if (meterHistory.size()>0) {
                        line.setMeter(meterHistory.get(0).getMeter());
                        line.setMeterRate(meterHistory.get(0).getFactor());
                        if (line.getMeterRate()!=null && line.getDelta()!=null)
                            line.setVal(line.getDelta() * line.getMeterRate());
                    }

                    line.setIsIgnore(false);
                    line.setIsBypassSection(false);
                    line.setBypassMeteringPoint(null);

                    if (bypassModes.size()>0) {
                        BypassMode bypassMode = bypassModes.get(0);
                        BypassModeValue bypassModeValue = bypassMode.getValues()
                            .stream()
                            .filter(v -> v.getParameter().equals(param))
                            .findFirst()
                            .orElse(null);

                        if (bypassModeValue!=null)
                            line.setBypassMeteringPoint(bypassMode.getBypassMeteringPoint());
                    }

                    if (undercounts.size()>0) {
                        Undercount undercount = undercounts.stream()
                            .filter(u -> u.getParameter().equals(param))
                            .findFirst()
                            .orElse(null);

                        if (undercount!=null)
                            line.setUndercount(undercount);
                    }

                    resultLines.add(line);
                }
            }

            balanceSubstResultMrLineRepo.save(resultLines);
            updateStatus(header, BatchStatusEnum.C);
            logger.info("Metering reading for header " + header.getId() + " completed");
        }

        catch (Exception e) {
            updateStatus(header, BatchStatusEnum.E);
            logger.error("Metering reading for header " + header.getId() + " terminated with exception");
            logger.error(e.toString() + ": " + e.getMessage());
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteLines(BalanceSubstResultHeader header) {
        List<BalanceSubstResultMrLine> lines = balanceSubstResultMrLineRepo.findAllByHeaderId(header.getId());
        for (int i=0; i<lines.size(); i++)
            balanceSubstResultMrLineRepo.delete(lines.get(i));
        balanceSubstResultMrLineRepo.flush();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateStatus(BalanceSubstResultHeader header, BatchStatusEnum status) {
        header.setStatus(status);
        balanceSubstResultHeaderRepo.save(header);
    }

    private BalanceSubstResultMrLine calcLine(BalanceSubstMrLine mrLine, Parameter parameter, CalcContext context) {
        BalanceSubstResultMrLine line = new BalanceSubstResultMrLine();

        DoubleExpression start = AtTimeValueExpression.builder()
            .meteringPointCode(mrLine.getMeteringPoint().getCode())
            .parameterCode(parameter.getCode())
            .rate(1d)
            .per("start")
            .context(context)
            .service(atTimeValueService)
            .build();

        DoubleExpression end  = AtTimeValueExpression.builder()
            .meteringPointCode(mrLine.getMeteringPoint().getCode())
            .parameterCode(parameter.getCode())
            .rate(1d)
            .per("end")
            .context(context)
            .service(atTimeValueService)
            .build();

        line.setStartVal(start.doubleValue());
        line.setEndVal(end.doubleValue());

        if (line.getStartVal() == null && line.getEndVal()==null)
            line.setDelta(null);
        else
            line.setDelta(Optional.ofNullable(line.getEndVal()).orElse(0d) - Optional.ofNullable(line.getStartVal()).orElse(0d));

        if (mrLine.getIsSection1()) line.setSection("1");
        if (mrLine.getIsSection2()) line.setSection("2");
        if (mrLine.getIsSection3()) line.setSection("3");
        if (mrLine.getIsSection4()) line.setSection("4");
        if (mrLine.getIsSection5()) line.setSection("5");

        return line;
    }
}
