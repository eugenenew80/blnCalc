package calc.formula.calculation;

import calc.entity.calc.*;
import calc.entity.calc.bs.BalanceSubstLine;
import calc.entity.calc.bs.BalanceSubstResultHeader;
import calc.entity.calc.bs.mr.BalanceSubstResultMrLine;
import calc.entity.calc.bs.ub.BalanceSubstResultUbLine;
import calc.entity.calc.bs.ub.BalanceSubstUbLine;
import calc.entity.calc.enums.LangEnum;
import calc.entity.calc.enums.ParamTypeEnum;
import calc.entity.calc.enums.PointTypeEnum;
import calc.formula.CalcContext;
import calc.formula.CalcResult;
import calc.formula.ContextType;
import calc.formula.expression.impl.MrExpression;
import calc.formula.expression.impl.PeriodTimeValueExpression;
import calc.formula.expression.impl.UavgExpression;
import calc.formula.expression.impl.WorkingHoursExpression;
import calc.formula.service.*;
import calc.repo.calc.BalanceSubstResultHeaderRepo;
import calc.repo.calc.BalanceSubstResultMrLineRepo;
import calc.repo.calc.BalanceSubstResultULineRepo;
import calc.repo.calc.BalanceSubstResultUbLineRepo;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.*;

import static calc.util.Util.round;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

@SuppressWarnings("Duplicates")
@Service
@RequiredArgsConstructor
public class BalanceSubstUbService {
    private static final Logger logger = LoggerFactory.getLogger(BalanceSubstUbService.class);
    private static final String docCode = "UNBALANCE";
    private final BalanceSubstResultHeaderRepo balanceSubstResultHeaderRepo;
    private final BalanceSubstResultMrLineRepo balanceSubstResultMrLineRepo;
    private final BalanceSubstResultUbLineRepo balanceSubstResultUbLineRepo;
    private final BalanceSubstResultULineRepo balanceSubstResultULineRepo;
    private final WorkingHoursService workingHoursService;
    private final BalanceSubstResultUService resultUService;
    private final MessageService messageService;
    private final ParamService paramService;
    private final CalcService calcService;
    private final PeriodTimeValueService periodTimeValueService;
    private Map<String, Parameter> mapParams = null;

    @PostConstruct
    public void init() {
        mapParams = paramService.getValues();
    }

    public boolean calc(BalanceSubstResultHeader header) {
        try {
            logger.info("Unbalance for balance with headerId " + header.getId() + " started");

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
                .defContextType(ContextType.DEFAULT)
                .values(new HashMap<>())
                .build();

            List<BalanceSubstResultMrLine> mrLines = balanceSubstResultMrLineRepo.findAllByHeaderId(header.getId());

            Double wApTotal = 0d; Double wAmTotal = 0d;
            for (BalanceSubstUbLine ubLine : header.getHeader().getUbLines()) {
                for (String direction : Arrays.asList("1", "2")) {
                    if (direction.equals("1") && !ubLine.getIsSection1()) continue;
                    if (direction.equals("2") && !ubLine.getIsSection2()) continue;
                    Parameter param = ofNullable(ubLine.getParam()).orElse(direction.equals("1") ? mapParams.get("A+") : mapParams.get("A-"));

                    Double w  = getMrVal(mrLines, ubLine, null, param, context);
                    if (direction.equals("1")) wApTotal += ofNullable(w).orElse(0d);
                    if (direction.equals("2")) wAmTotal += ofNullable(w).orElse(0d);
                }
            }

            List<BalanceSubstResultUbLine> lines = new ArrayList<>();
            for (BalanceSubstUbLine ubLine : header.getHeader().getUbLines()) {
                MeteringPoint meteringPoint = ubLine.getMeteringPoint();
                if (meteringPoint == null)
                    continue;

                String info = meteringPoint.getCode();
                Map<String, String> params = new HashMap<>();
                params.put("point", meteringPoint.getCode());

                Double workHours = WorkingHoursExpression.builder()
                    .objectType("mp")
                    .objectId(meteringPoint.getId())
                    .service(workingHoursService)
                    .context(context)
                    .build()
                    .doubleValue();

                workHours = ofNullable(workHours).orElse(0d);
                workHours = round(workHours,1);

                if (workHours.equals(0d)) {
                    messageService.addMessage(header, ubLine.getId(), docCode, "UB_WORK_HOURS_NOT_FOUND", info);
                    continue;
                }

                MeteringPoint inputMp = balanceSubstResultULineRepo.findAllByHeaderId(header.getId())
                    .stream()
                    .filter(t -> t.getMeteringPoint().getVoltageClass()!=null)
                    .filter(t -> t.getMeteringPoint().getVoltageClass().equals(meteringPoint.getVoltageClass()))
                    .map(t -> t.getMeteringPoint())
                    .findFirst()
                    .orElse(meteringPoint);

                if (inputMp.getVoltageClass() == null) {
                    messageService.addMessage(header, ubLine.getId(), docCode, "UB_VOLTAGE_CLASS_NOT_FOUND", info);
                    continue;
                }

                Double uAvg = UavgExpression.builder()
                    .meteringPointCode(inputMp.getCode())
                    .def(inputMp.getVoltageClass().getValue() / 1000d)
                    .context(context)
                    .service(resultUService)
                    .build()
                    .doubleValue();

                Parameter parU = paramService.getValues().get("U");
                uAvg = round(uAvg, parU);
                uAvg = Optional.of(uAvg).orElse(0d);

                if (uAvg.equals(0d)) {
                    messageService.addMessage(header, ubLine.getId(), docCode, "UB_UAVG_NOT_FOUND", info);
                    continue;
                }

                List<MeterHistory> meterHistories = mrLines.stream()
                    .filter(t -> t.getMeteringPoint().equals(meteringPoint))
                    .filter(t -> t.getMeterHistory() != null)
                    .filter(t -> t.getBypassMeteringPoint() == null)
                    .filter(t -> !t.getIsIgnore())
                    .map(t -> t.getMeterHistory())
                    .distinct()
                    .collect(toList());

                for (String direction : Arrays.asList("1", "2")) {
                    if (direction.equals("1") && !ubLine.getIsSection1()) continue;
                    if (direction.equals("2") && !ubLine.getIsSection2()) continue;
                    Parameter param = ofNullable(ubLine.getParam()).orElse(direction.equals("1") ? mapParams.get("A+") : mapParams.get("A-"));

                    if (meterHistories.size() == 0) {
                        Double w  = getMrVal(mrLines, ubLine, null, param, context);
                        Double wa = getMrVal(mrLines, ubLine, null, mapParams.get("A+"), context) + getMrVal(mrLines, ubLine, null, mapParams.get("A-"), context);
                        Double wr = getMrVal(mrLines, ubLine, null, mapParams.get("R+"), context) + getMrVal(mrLines, ubLine, null, mapParams.get("R-"), context);

                        BalanceSubstResultUbLine line = new BalanceSubstResultUbLine();
                        line.setHeader(header);
                        line.setMeteringPoint(meteringPoint);
                        line.setDirection(direction);
                        line.setW(w);
                        line.setWa(wa);
                        line.setWr(wr);
                        lines.add(line);
                    }

                    for (MeterHistory meterHistory : meterHistories) {
                        TtType ttType = meterHistory.getTtType();
                        TnType tnType = meterHistory.getTnType();
                        EemType eemType = meterHistory.getMeter().getEemType();

                        if (eemType == null) {
                            messageService.addMessage(header, ubLine.getId(), docCode, "UB_EEM_TYPE_NOT_FOUND", info);
                            continue;
                        }

                        if (eemType.getAccuracyClass() == null) {
                            messageService.addMessage(header, ubLine.getId(), docCode, "UB_ACCURACY_CLASS_NOT_FOUND", info);
                            continue;
                        }

                        if (ttType == null) {
                            messageService.addMessage(header, ubLine.getId(), docCode, "UB_TT_TYPE_NOT_FOUND", info);
                            continue;
                        }

                        if (ttType.getRatedCurrent1() == null) {
                            messageService.addMessage(header, ubLine.getId(), docCode, "UB_RATED_CURRENT_NOT_FOUND", info);
                            continue;
                        }

                        if (ttType.getAccuracyClass() == null) {
                            messageService.addMessage(header, ubLine.getId(), docCode, "UB_ACCURACY_CLASS_NOT_FOUND", info);
                            continue;
                        }

                        if (tnType == null && !meterHistory.getIsTnDirectInclusion()) {
                            messageService.addMessage(header, ubLine.getId(), docCode, "UB_TN_TYPE_NOT_FOUND", info);
                            continue;
                        }

                        if (tnType != null && tnType.getAccuracyClass() == null) {
                            messageService.addMessage(header, ubLine.getId(), docCode, "UB_ACCURACY_CLASS_NOT_FOUND", info);
                            continue;
                        }

                        Double w  = getMrVal(mrLines, ubLine, meterHistory, param, context);
                        Double wa = getMrVal(mrLines, ubLine, meterHistory, mapParams.get("A+"), context) + getMrVal(mrLines, ubLine, meterHistory, mapParams.get("A-"), context);
                        Double wr = getMrVal(mrLines, ubLine, meterHistory, mapParams.get("R+"), context) + getMrVal(mrLines, ubLine, meterHistory, mapParams.get("R-"), context);

                        Double i1avgVal = Math.sqrt(Math.pow(wa, 2) + Math.pow(wr, 2)) / (1.73d * uAvg * workHours);
                        Double i1avgProc = i1avgVal / ttType.getRatedCurrent1() * 100d;
                        Double ttAcProc = ttType.getAccuracyClass().getValue();

                        Pair<Double, String> bttProcResult = getBttProc(ttType.getAccuracyClass(), i1avgProc);
                        Double bttProc = bttProcResult.getFirst();

                        if (!bttProcResult.getSecond().equals("OK")) {
                            messageService.addMessage(header, ubLine.getId(), docCode, bttProcResult.getSecond(), params);
                            bttProc = 0d;
                        }

                        Double buProc = 0d;
                        if (tnType != null && tnType.getAccuracyClass() != null)
                            buProc = ofNullable(tnType.getAccuracyClass().getValue()).orElse(0d);

                        Double ttNumber = ofNullable(meterHistory.getTtNumber()).orElse(1d);
                        Double biProc = bttProc * Math.sqrt(ttNumber);
                        Double blProc = buProc <= 0.5 ? 0.25 : 0.5;
                        Double bsoProc = ofNullable(eemType.getAccuracyClass().getValue()).orElse(0d);
                        Double bProc = Math.sqrt(Math.pow(biProc, 2) + Math.pow(buProc, 2) + Math.pow(blProc, 2) + Math.pow(bsoProc, 2)) * 1.1d;

                        Double dol = 0d;
                        if (direction.equals("1") && !wApTotal.equals(0d)) dol = w / wApTotal;
                        if (direction.equals("2") && !wAmTotal.equals(0d)) dol = w / wAmTotal;
                        Double b2dol2 = Math.pow(bProc / 100d, 2) * Math.pow(dol, 2);

                        BalanceSubstResultUbLine line = new BalanceSubstResultUbLine();
                        line.setHeader(header);
                        line.setMeteringPoint(meteringPoint);
                        line.setDirection(direction);
                        line.setW(w);
                        line.setWa(wa);
                        line.setWr(wr);
                        line.setTtStar(meterHistory.getTtMountedOn());
                        line.setTtacProc(ttAcProc);
                        line.setI1Nom(ttType.getRatedCurrent1());
                        line.setTRab(workHours);
                        line.setUavg(uAvg);
                        line.setI1avgVal(i1avgVal);
                        line.setI1avgProc(i1avgProc);
                        line.setBttFactor(null);
                        line.setBttProc(bttProc);
                        line.setBiProc(biProc);
                        line.setBuProc(buProc);
                        line.setBlProc(blProc);
                        line.setBsoProc(bsoProc);
                        line.setBProc(bProc);
                        line.setDol(dol);
                        line.setB2dol2(b2dol2);
                        lines.add(line);
                    }
                }
            }

            Double r1SumW = lines.stream()
                .filter(t -> t.getDirection().equals("1"))
                .filter(t -> t.getW() != null)
                .map(t -> t.getW())
                .reduce((t1, t2) -> t1 + t2)
                .orElse(0d);

            Double r1SumB2D2 = lines.stream()
                .filter(t -> t.getDirection().equals("1"))
                .filter(t -> t.getB2dol2() != null)
                .map(t -> t.getB2dol2())
                .reduce((t1, t2) -> t1 + t2)
                .orElse(0d);

            Double r2SumB2D2 = lines.stream()
                .filter(t -> t.getDirection().equals("2"))
                .filter(t -> t.getB2dol2() != null)
                .map(t -> t.getB2dol2())
                .reduce((t1, t2) -> t1 + t2)
                .orElse(0d);

            Double nbdProc = Math.sqrt(r1SumB2D2 + r2SumB2D2) * 100d;
            Double nbdVal = nbdProc * r1SumW / 100d;

            nbdVal = round(nbdVal, paramService.getValues().get("A+"));

            header.setNbdProc(nbdProc);
            header.setNbdVal(nbdVal);

            deleteLines(header);
            saveLines(lines);
            balanceSubstResultHeaderRepo.save(header);

            logger.info("Unbalance for balance with headerId " + header.getId() + " completed");
            return true;
        }

        catch (Exception e) {
            messageService.addMessage(header, null,  docCode,"RUNTIME_EXCEPTION", e.getClass().getCanonicalName());
            logger.error("Unbalance for balance with headerId " + header.getId() + " terminated with exception: " + e.toString() + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private Pair<Double, String> getBttProc(AccuracyClass accuracyClass, Double i1avgProc) {
        Double bttProc = Double.MIN_VALUE;
        String msgCode = "OK";
        String designation = accuracyClass.getDesignation();

        if (designation.equals("0.1")) {
            if (i1avgProc > 120)        msgCode = "UB_I1_AVG_PROC_MORE_THAN_120";
            else if (i1avgProc >= 100)  bttProc = accuracyClass.getValue();
            else if (i1avgProc >= 20)   bttProc = 0.225d - 0.00125d * i1avgProc;
            else if (i1avgProc >= 5)    bttProc = 0.46666d - 0.01333d * i1avgProc;
            else if (i1avgProc >= 1)    bttProc = 0.4d;
            else if (i1avgProc >= 0)    bttProc = 0.4d;
            else                        msgCode = "UB_I1_AVG_PROC_NEGATIVE";
        }

        else if (designation.equals("0.2S")) {
            if (i1avgProc > 120)        msgCode = "UB_I1_AVG_PROC_MORE_THAN_120";
            else if (i1avgProc >= 100)  bttProc = accuracyClass.getValue();
            else if (i1avgProc >= 20)   bttProc = accuracyClass.getValue();
            else if (i1avgProc >= 5)    bttProc = 0.4d - 0.01d * i1avgProc;
            else if (i1avgProc >= 1)    bttProc = 0.85d - 0.1d * i1avgProc;
            else if (i1avgProc >= 0)    bttProc = 0.75d;
            else                        msgCode = "UB_I1_AVG_PROC_NEGATIVE";
        }

        else if (designation.equals("0.2")) {
            if (i1avgProc > 120)        msgCode = "UB_I1_AVG_PROC_MORE_THAN_120";
            else if (i1avgProc >= 100)  bttProc = accuracyClass.getValue();
            else if (i1avgProc >= 20)   bttProc = 0.3875d - 0.001875d * i1avgProc;
            else if (i1avgProc >= 5)    bttProc = (13.25d - 0.4d * i1avgProc) / 15d;
            else if (i1avgProc >= 1)    bttProc = 1.6875d - 0.1875d * i1avgProc;
            else if (i1avgProc >= 0)    bttProc = 1.5d;
            else                        msgCode = "UB_I1_AVG_PROC_NEGATIVE";
        }

        else if (designation.equals("0.5S")) {
            if (i1avgProc > 120)        msgCode = "UB_I1_AVG_PROC_MORE_THAN_120";
            else if (i1avgProc >= 100)  bttProc = accuracyClass.getValue();
            else if (i1avgProc >= 20)   bttProc = accuracyClass.getValue();
            else if (i1avgProc >= 5)    bttProc = (12.5d - 0.25d * i1avgProc) / 15d;
            else if (i1avgProc >= 1)    bttProc = 1.6875d - 0.1875d * i1avgProc;
            else if (i1avgProc >= 0)    bttProc = 1.5d;
            else                        msgCode = "UB_I1_AVG_PROC_NEGATIVE";
        }

        else if (designation.equals("0.5")) {
            if (i1avgProc > 120)        msgCode = "UB_I1_AVG_PROC_MORE_THAN_120";
            else if (i1avgProc >= 100)  bttProc = accuracyClass.getValue();
            else if (i1avgProc >= 20)   bttProc = 0.8125d - 0.003125d * i1avgProc;
            else if (i1avgProc >= 5)    bttProc = 1.75d - 0.05d * i1avgProc;
            else if (i1avgProc >= 1)    bttProc = 1.5d;
            else if (i1avgProc >=0)     bttProc = 1.5d;
            else                        msgCode = "UB_I1_AVG_PROC_NEGATIVE";
        }

        else if (designation.equals("1")) {
            if (i1avgProc > 120)        msgCode = "UB_I1_AVG_PROC_MORE_THAN_120";
            else if (i1avgProc >= 100)  bttProc = accuracyClass.getValue();
            else if (i1avgProc >= 20)   bttProc = 1.625d - 0.00625d * i1avgProc;
            else if (i1avgProc >= 5)    bttProc = 3.5d - 0.1d * i1avgProc;
            else if (i1avgProc >= 1)    bttProc = 3d;
            else if (i1avgProc >= 0)    bttProc = 3d;
            else                        msgCode = "UB_I1_AVG_PROC_NEGATIVE";
        }

        else if (designation.equals("3") || designation.equals("5") || designation.equals("10")) {
            if (i1avgProc > 120)        msgCode = "UB_I1_AVG_PROC_MORE_THAN_120";
            else if (i1avgProc >= 100)  bttProc = accuracyClass.getValue();
            else if (i1avgProc >= 20)   bttProc = accuracyClass.getValue();
            else if (i1avgProc >= 5)    bttProc = accuracyClass.getValue();
            else if (i1avgProc >= 1)    bttProc = accuracyClass.getValue();
            else if (i1avgProc >= 0)    bttProc = accuracyClass.getValue();
            else                        msgCode = "UB_I1_AVG_PROC_NEGATIVE";
        }
        else
            msgCode = "UB_ACCURACY_CLASS_UNKNOWN";

        return Pair.of(bttProc, msgCode);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void saveLines(List<BalanceSubstResultUbLine> lines) {
        balanceSubstResultUbLineRepo.save(lines);
        balanceSubstResultUbLineRepo.flush();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void deleteLines(BalanceSubstResultHeader header) {
        List<BalanceSubstResultUbLine> lines = balanceSubstResultUbLineRepo.findAllByHeaderId(header.getId());
        for (int i=0; i<lines.size(); i++)
            balanceSubstResultUbLineRepo.delete(lines.get(i));
        balanceSubstResultUbLineRepo.flush();
    }

    private Double getMrVal(List<BalanceSubstResultMrLine> mrLines, BalanceSubstUbLine ubLine, MeterHistory meterHistory, Parameter param, CalcContext context) throws Exception {
        if (ubLine.getMeteringPoint().getPointType() == PointTypeEnum.PMP) {
            Double val = mrLines.stream()
                .filter(t -> t.getMeteringPoint().equals(ubLine.getMeteringPoint()))
                .filter(t -> t.getParam().equals(param))
                .filter(t -> t.getMeterHistory() != null)
                .filter(t -> meterHistory == null || t.getMeterHistory().equals(meterHistory))
                .filter(t -> t.getBypassMeteringPoint() == null)
                .filter(t -> !t.getIsIgnore())
                .map(t -> ofNullable(t.getVal()).orElse(0d) + ofNullable(t.getUnderCountVal()).orElse(0d))
                .reduce((t1, t2) -> t1 + t2)
                .orElse(0d);

            return val * ofNullable(ubLine.getRate()).orElse(1d);
        }

        Double val = PeriodTimeValueExpression.builder()
            .meteringPointCode(ubLine.getMeteringPoint().getCode())
            .parameterCode(param.getCode())
            .rate(1d)
            .startHour((byte) 0)
            .endHour((byte) 23)
            .periodType(context.getPeriodType())
            .context(context)
            .service(periodTimeValueService)
            .build()
            .doubleValue();

        if (ofNullable(val).orElse(0d) == 0d) {
            CalcResult result = calcService.calcMeteringPoint(ubLine.getMeteringPoint(), param, ParamTypeEnum.PT, context);
            val = result!=null ? result.getDoubleValue() : null;
            if (val != null)
                val = val * ofNullable(ubLine.getRate()).orElse(1d);
        }

        return ofNullable(val).orElse(0d);
    }
}