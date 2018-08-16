package calc.formula.doc.impl;

import calc.entity.calc.*;
import calc.entity.calc.enums.BatchStatusEnum;
import calc.entity.calc.enums.ParamTypeEnum;
import calc.formula.CalcContext;
import calc.formula.CalcResult;
import calc.formula.service.CalcService;
import calc.repo.calc.BalanceSubstResultHeaderRepo;
import calc.repo.calc.BalanceSubstResultMrLineRepo;
import calc.repo.calc.ParameterRepo;
import calc.repo.calc.UnitRepo;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@SuppressWarnings("ALL")
@Service
@RequiredArgsConstructor
public class BalanceSubstMrServiceImpl {
    private static final Logger logger = LoggerFactory.getLogger(BalanceSubstMrServiceImpl.class);
    private final CalcService calcService;
    private final BalanceSubstResultHeaderRepo balanceSubstResultHeaderRepo;
    private final BalanceSubstResultMrLineRepo balanceSubstResultMrLineRepo;
    private final ParameterRepo parameterRepo;
    private final UnitRepo unitRepo;

    public void calc(BalanceSubstResultHeader header)  {
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
                .values(new ArrayList<>())
                .build();

            List<BalanceSubstResultMrLine> resultLines = new ArrayList<>();
            List<BalanceSubstMrLine> mrLines = header.getHeader().getMrLines();
            for (BalanceSubstMrLine mrLine : mrLines) {
                List<MeterHistory> meters = mrLine.getMeteringPoint().getMeters();
                for (Parameter param : Arrays.asList(parAp, parAm, parRp, parRm)) {
                    Map<String, Formula> formulas = createFormulas(mrLine, param);
                    BalanceSubstResultMrLine line = calcLine(formulas, context);

                    if (mrLine.getIsSection1()) line.setSection("1");
                    if (mrLine.getIsSection2()) line.setSection("2");
                    if (mrLine.getIsSection3()) line.setSection("3");
                    if (mrLine.getIsSection4()) line.setSection("4");
                    if (mrLine.getIsSection5()) line.setSection("5");

                    line.setHeader(header);
                    line.setMeteringPoint(mrLine.getMeteringPoint());
                    line.setParam(param);
                    line.setUnit(aUnitCode);
                    line.setStartMeteringDate(context.getStartDate().atStartOfDay());
                    line.setEndMeteringDate(context.getEndDate().atStartOfDay().plusDays(1));

                    if (line.getStartVal() == null && line.getEndVal()==null)
                        line.setDelta(null);

                    if (meters.size()>0) {
                        line.setMeter(meters.get(0).getMeter());
                        line.setMeterRate(meters.get(0).getFactor());
                        if (line.getMeterRate()!=null && line.getDelta()!=null)
                            line.setVal(line.getDelta() * line.getMeterRate());
                    }

                    line.setIsIgnore(false);
                    line.setIsBypassSection(false);
                    line.setBypassMeteringPoint(null);

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

    private BalanceSubstResultMrLine calcLine(Map<String, Formula> formulas, CalcContext context) {
        BalanceSubstResultMrLine line = new BalanceSubstResultMrLine();

        for (String key : formulas.keySet()) {
            Formula formula = formulas.get(key);
            String formulaText = calcService.formulaToString(formula);
            System.out.println(formulaText);
            try {

                CalcResult result = calcService.calc(formulaText, context);
                if (key.equals("start-val"))
                    line.setStartVal(result.getDoubleVal());

                if (key.equals("end-val"))
                    line.setEndVal(result.getDoubleVal());

                if (key.equals("delta"))
                    line.setDelta(result.getDoubleVal());
            }
            catch (Exception e) {
                logger.error("ERROR: " + e.toString() + ", " + e.getMessage());
            }
        }

        return line;
    }

    private Map<String, Formula> createFormulas(BalanceSubstMrLine mrLine, Parameter parameter) {
        Map<String, Formula> map = new HashMap<>();
        map.putIfAbsent("start-val", calcService.createFormula(mrLine.getMeteringPoint(), parameter, ParamTypeEnum.ATS));
        map.putIfAbsent("end-val",  calcService.createFormula(mrLine.getMeteringPoint(), parameter, ParamTypeEnum.ATE));
        map.putIfAbsent("delta",    calcService.createFormula(mrLine.getMeteringPoint(), parameter, ParamTypeEnum.DELTA));
        return map;
    }
}
