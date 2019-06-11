package calc.formula.calculation;

import calc.entity.calc.*;
import calc.entity.calc.bs.*;
import calc.entity.calc.bs.mr.*;
import calc.entity.calc.bs.ub.*;
import calc.entity.calc.enums.*;
import calc.formula.*;
import calc.formula.expression.impl.*;
import calc.formula.service.*;
import calc.repo.calc.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import static calc.util.Util.*;
import static java.lang.Math.*;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

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

    public boolean calc(BalanceSubstResultHeader header) {
        try {
            logger.info("Unbalance for balance with headerId " + header.getId() + " started");

            CalcContext context = CalcContext.builder()
                .lang(LangEnum.RU)
                .header(header)
                .defContextType(ContextTypeEnum.DEFAULT)
                .build();

            List<BalanceSubstResultMrLine> mrLines = balanceSubstResultMrLineRepo.findAllByHeaderId(header.getId());

            Double wApTotal = 0d; Double wAmTotal = 0d;
            for (BalanceSubstUbLine ubLine : header.getHeader().getUbLines()) {
                for (String direction : Arrays.asList("1", "2")) {
                    if (direction.equals("1") && !ubLine.getIsSection1()) continue;
                    if (direction.equals("2") && !ubLine.getIsSection2()) continue;

                    Parameter param = ofNullable(ubLine.getParam()).orElse(direction.equals("1")
                        ? paramService.getParam("A+")
                        : paramService.getParam("A-"));

                    Double w  = getMrVal(mrLines, ubLine, null, param, context);
                    if (direction.equals("1")) wApTotal += ofNullable(w).orElse(0d);
                    if (direction.equals("2")) wAmTotal += ofNullable(w).orElse(0d);
                }
            }

            List<BalanceSubstResultUbLine> results = new ArrayList<>();
            for (BalanceSubstUbLine ubLine : header.getHeader().getUbLines()) {
                MeteringPoint meteringPoint = ubLine.getMeteringPoint();
                if (meteringPoint == null)
                    continue;

                String info = meteringPoint.getCode();
                Map<String, String> params = new HashMap<>();
                params.put("point", meteringPoint.getCode());

                Double workHoursMp = WorkingHoursExpression.builder()
                    .objectType("mp")
                    .objectId(meteringPoint.getId())
                    .service(workingHoursService)
                    .context(context)
                    .build()
                    .doubleValue();

                workHoursMp = ofNullable(workHoursMp).orElse(0d);
                workHoursMp = round(workHoursMp,1);

                Double workHours = workHoursMp;

                if (workHours.equals(0d))
                    messageService.addMessage(header, ubLine.getId(), docCode, "UB_WORK_HOURS_NOT_FOUND", info);

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

                Parameter parU = paramService.getParam("U");
                uAvg = round(uAvg, parU);
                uAvg = Optional.of(uAvg).orElse(0d);

                if (uAvg.equals(0d)) {
                    messageService.addMessage(header, ubLine.getId(), docCode, "UB_UAVG_NOT_FOUND", info);
                    continue;
                }

                List<MeterHistory> meterHistories = mrLines.stream()
                    .filter(t -> t.getMeteringPoint().equals(meteringPoint))
                    .filter(t -> t.getMeterHistory() != null)
                    //.filter(t -> t.getBypassMeteringPoint() == null)
                    .filter(t -> !t.getIsIgnore())
                    .map(t -> t.getMeterHistory())
                    .distinct()
                    .collect(toList());

                for (String direction : Arrays.asList("1", "2")) {
                    if (direction.equals("1") && !ubLine.getIsSection1()) continue;
                    if (direction.equals("2") && !ubLine.getIsSection2()) continue;

                    Parameter paramA = ofNullable(ubLine.getParam()).orElse(direction.equals("1")
                        ? paramService.getParam("A+")
                        : paramService.getParam("A-"));

                    Parameter paramR = paramA.getCode().equals("A+")
                        ? paramService.getParam("R+")
                        : paramService.getParam("R-");

                    if (meterHistories.size() == 0) {
                        Double w  = getMrVal(mrLines, ubLine, null, paramA, context);
                        Double wa = getMrVal(mrLines, ubLine, null, paramA, context);
                        Double wr = getMrVal(mrLines, ubLine, null, paramR, context);

                        BalanceSubstResultUbLine line = new BalanceSubstResultUbLine();
                        line.setHeader(header);
                        line.setMeteringPoint(meteringPoint);
                        line.setDirection(direction);
                        line.setW(w);
                        line.setWa(wa);
                        line.setWr(wr);
                        results.add(line);
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


                        List<BalanceSubstResultMrLine> bpMrLines = mrLines.stream()
                            .filter(t -> t.getMeteringPoint().equals(meteringPoint))
                            .filter(t -> t.getParam().equals(paramA))
                            .filter(t -> t.getBypassMeteringPoint() != null)
                            .filter(t -> t.getMeterHistory() != null)
                            .filter(t -> t.getMeterHistory().equals(meterHistory))
                            .collect(toList());

                        MeteringPoint bpMeteringPoint = null;
                        workHours = workHoursMp;
                        if (!bpMrLines.isEmpty()) {
                            logger.trace(" work hours calc for bypass metering point started");
                            workHours = 0d;
                            for (BalanceSubstResultMrLine bpMrLine : bpMrLines) {
                                bpMeteringPoint = bpMrLine.getBypassMeteringPoint();

                                LocalDateTime bpStartMeteringDate = bpMrLine.getStartMeteringDate();
                                LocalDateTime bpEndMeteringDate = bpMrLine.getEndMeteringDate();

                                Duration duration = Duration.between(bpStartMeteringDate, bpEndMeteringDate);
                                Double h = duration.getSeconds() / 3600d;

                                logger.trace(" startDateTime: " + bpStartMeteringDate);
                                logger.trace(" endDateTime: " + bpEndMeteringDate);
                                logger.trace(" h: " + h);

                                workHours += h;
                            }
                            logger.trace(" work hours calc for bypass metering point complteted: " + workHours);
                        }

                        Long ttNumber = ofNullable(meterHistory.getTtNumber()).orElse(1l);

                        Double w  = getMrVal(mrLines, ubLine, meterHistory, paramA, context);
                        Double wa = getMrVal(mrLines, ubLine, meterHistory, paramA, context);
                        Double wr = getMrVal(mrLines, ubLine, meterHistory, paramR, context);

                        Double i1avgVal = 0d;
                        if (workHours != 0d)
                            i1avgVal = sqrt(pow(wa, 2) + pow(wr, 2)) / (sqrt(3d) * uAvg * workHours);

                        i1avgVal = i1avgVal / (ttNumber.equals(0l) ? 1 : ttNumber);

                        Double i1avgProc = i1avgVal / ttType.getRatedCurrent1() * 100d;
                        Double ttAcProc = ttType.getAccuracyClass().getValue();

                        Pair<Double, String> bttProcResult = getBttProc(ttType.getAccuracyClass(), i1avgProc);
                        Double bttProc = bttProcResult.getFirst();

                        if (!bttProcResult.getSecond().equals("OK")) {
                            messageService.addMessage(header, ubLine.getId(), docCode, bttProcResult.getSecond(), params);
                            bttProc = 0d;
                        }

                        Double buProc = null, blProc = null;
                        if (tnType != null) {
                            if (tnType.getAccuracyClass() != null)
                                buProc = ofNullable(tnType.getAccuracyClass().getValue()).orElse(0d);

                            if (buProc != null)
                                blProc = buProc <= 0.5 ? 0.25 : 0.5;
                        }


                        String ttMountedOn = ttNumber > 1l ? meterHistory.getTtMountedOn() : null;
                        Double biProc = ttType != null ? bttProc * sqrt(ttNumber) : null;
                        Double bsoProc = ofNullable(eemType.getAccuracyClass().getValue()).orElse(0d);

                        Double bProc = sqrt(
                                pow(ofNullable(biProc).orElse(0d), 2)
                            +   pow(ofNullable(buProc).orElse(0d), 2)
                            +   pow(ofNullable(blProc).orElse(0d), 2)
                            +   pow(ofNullable(bsoProc).orElse(0d), 2)
                        ) * 1.1d;

                        Double dol = 0d;
                        if (direction.equals("1") && !wApTotal.equals(0d)) dol = w / wApTotal;
                        if (direction.equals("2") && !wAmTotal.equals(0d)) dol = w / wAmTotal;
                        Double b2dol2 = pow(bProc / 100d, 2) * pow(dol, 2);

                        BalanceSubstResultUbLine result = new BalanceSubstResultUbLine();
                        result.setHeader(header);
                        result.setMeteringPoint(meteringPoint);
                        result.setBypassMeteringPoint(bpMeteringPoint);
                        result.setDirection(direction);
                        result.setW(w);
                        result.setWa(wa);
                        result.setWr(wr);
                        result.setTtStar(ttMountedOn);
                        result.setTtacProc(ttAcProc);
                        result.setI1Nom(ttType.getRatedCurrent1());
                        result.setTRab(workHours);
                        result.setUavg(uAvg);
                        result.setI1avgVal(i1avgVal);
                        result.setI1avgProc(i1avgProc);
                        result.setBttFactor(null);
                        result.setBttProc(bttProc);
                        result.setBiProc(biProc);
                        result.setBuProc(buProc);
                        result.setBlProc(blProc);
                        result.setBsoProc(bsoProc);
                        result.setBProc(bProc);
                        result.setDol(dol);
                        result.setB2dol2(b2dol2);

                        if (tnType != null)
                            result.setTnAccuracyClass(tnType.getAccuracyClass());

                        if (ttType != null)
                            result.setTtAccuracyClass(ttType.getAccuracyClass());

                        if (eemType != null)
                            result.setMeterAccuracyClass(eemType.getAccuracyClass());

                        results.add(result);
                    }
                }
            }

            Double r1SumW = results.stream()
                .filter(t -> t.getDirection().equals("1"))
                .filter(t -> t.getW() != null)
                .map(t -> t.getW())
                .reduce((t1, t2) -> t1 + t2)
                .orElse(0d);

            Double r1SumB2D2 = results.stream()
                .filter(t -> t.getDirection().equals("1"))
                .filter(t -> t.getB2dol2() != null)
                .map(t -> t.getB2dol2())
                .reduce((t1, t2) -> t1 + t2)
                .orElse(0d);

            Double r2SumB2D2 = results.stream()
                .filter(t -> t.getDirection().equals("2"))
                .filter(t -> t.getB2dol2() != null)
                .map(t -> t.getB2dol2())
                .reduce((t1, t2) -> t1 + t2)
                .orElse(0d);

            Double nbdProc = sqrt(r1SumB2D2 + r2SumB2D2) * 100d;
            Double nbdVal = nbdProc * r1SumW / 100d;

            nbdVal = round(nbdVal,paramService.getParam("A+"));

            header.setNbdProc(nbdProc);
            header.setNbdVal(nbdVal);

            deleteLines(header);
            saveLines(results);
            balanceSubstResultHeaderRepo.save(header);

            logger.info("Unbalance for balance with headerId " + header.getId() + " completed");
            return true;
        }

        catch (Exception e) {
            logger.error("Unbalance for balance with headerId " + header.getId() + " terminated with exception: " + e.toString() + ": " + e.getMessage());
            e.printStackTrace();

            messageService.addMessage(header, null,  docCode,"RUNTIME_EXCEPTION", buildMsgParams(e));
            return false;
        }
    }

    @SuppressWarnings("Duplicates")
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

    private Double getMrVal(List<BalanceSubstResultMrLine> mrLines, BalanceSubstUbLine ubLine, MeterHistory meterHistory, Parameter param, CalcContext context) throws Exception {
        MeteringPoint meteringPoint = ubLine.getMeteringPoint();

        if (meteringPoint.getPointType() == PointTypeEnum.PMP) {
            Double val = mrLines.stream()
                .filter(t -> t.getMeteringPoint().equals(meteringPoint))
                .filter(t -> t.getParam().equals(param))
                .filter(t -> t.getMeterHistory() != null)
                .filter(t -> t.getMeterHistory().equals(meterHistory))
                //.filter(t -> t.getBypassMeteringPoint() == null)
                .filter(t -> !t.getIsIgnore())
                .map(t -> ofNullable(t.getVal()).orElse(0d) + ofNullable(t.getUnderCountVal()).orElse(0d))
                .reduce((t1, t2) -> t1 + t2)
                .orElse(0d);

            return val * ofNullable(ubLine.getRate()).orElse(1d);
        }

        CalcProperty property = CalcProperty.builder()
            .contextType(context.getDefContextType())
            .processOrder(ProcessOrderEnum.READ_CALC)
            .build();

        CalcResult result = calcService.calcValue(meteringPoint, param, context, property);
        Double val = result!=null ? result.getDoubleValue() : null;
        if (val != null)
            val = val * ofNullable(ubLine.getRate()).orElse(1d);

        return ofNullable(val).orElse(0d);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 60)
    void saveLines(List<BalanceSubstResultUbLine> lines) {
        balanceSubstResultUbLineRepo.save(lines);
        balanceSubstResultUbLineRepo.flush();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 60)
    void deleteLines(BalanceSubstResultHeader header) {
        List<BalanceSubstResultUbLine> lines = balanceSubstResultUbLineRepo.findAllByHeaderId(header.getId());
        balanceSubstResultUbLineRepo.delete(lines);
        balanceSubstResultUbLineRepo.flush();
    }
}