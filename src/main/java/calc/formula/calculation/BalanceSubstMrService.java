package calc.formula.calculation;

import calc.entity.calc.*;
import calc.entity.calc.bs.BalanceSubstMrLine;
import calc.entity.calc.bs.BalanceSubstResultHeader;
import calc.entity.calc.bs.BalanceSubstResultMrLine;
import calc.entity.calc.enums.BatchStatusEnum;
import calc.formula.CalcContext;
import calc.formula.service.MessageService;
import calc.formula.service.MeteringReading;
import calc.formula.service.MrService;
import calc.repo.calc.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.PostConstruct;
import java.util.*;

@SuppressWarnings("Duplicates")
@Service
@RequiredArgsConstructor
public class BalanceSubstMrService {
    private static final Logger logger = LoggerFactory.getLogger(BalanceSubstMrService.class);
    private final BalanceSubstResultHeaderRepo balanceSubstResultHeaderRepo;
    private final BalanceSubstResultMrLineRepo balanceSubstResultMrLineRepo;
    private final MrService mrService;
    private final ParameterRepo parameterRepo;
    private final MessageService messageService;
    private static final String docCode = "ACT";
    private Map<String, Parameter> mapParams = null;

    @PostConstruct
    public void init() {
        mapParams = new HashMap<>();
        mapParams.put("A-", parameterRepo.findByCode("A-"));
        mapParams.put("A+", parameterRepo.findByCode("A+"));
        mapParams.put("R-", parameterRepo.findByCode("R-"));
        mapParams.put("R+", parameterRepo.findByCode("R+"));
    }

    public boolean calc(BalanceSubstResultHeader header) {
        try {
            logger.info("Metering reading for header " + header.getId() + " started");
            header = balanceSubstResultHeaderRepo.findOne(header.getId());
            if (header.getStatus() == BatchStatusEnum.E)
                return false;

            updateStatus(header, BatchStatusEnum.P);
            deleteLines(header);
            deleteMessages(header, docCode);

            CalcContext context = CalcContext.builder()
                .docCode(docCode)
                .docId(header.getId())
                .startDate(header.getStartDate())
                .endDate(header.getEndDate())
                .orgId(header.getOrganization().getId())
                .energyObjectType("SUBSTATION")
                .energyObjectId(header.getSubstation().getId())
                .trace(new HashMap<>())
                .values(new HashMap<>())
                .build();

            List<BalanceSubstResultMrLine> resultLines = new ArrayList<>();
            for (BalanceSubstMrLine mrLine : header.getHeader().getMrLines()) {
                List<MeteringReading> meteringReadings = mrService.calc(mrLine.getMeteringPoint(), context);

                for (MeteringReading t : meteringReadings) {
                    BalanceSubstResultMrLine line = new BalanceSubstResultMrLine();

                    if (t.getMeter() == null)
                        messageService.addMessage(header, mrLine.getId(), docCode, "MR_METER_NOT_FOUND");

                    if (t.getMeterHistory() == null)
                        messageService.addMessage(header, mrLine.getId(), docCode, "MR_METER_HISTORY_NOT_FOUND");

                    String section = getSection(mrLine);
                    if (section == null || section.equals("")) {
                        messageService.addMessage(header, mrLine.getId(), docCode, "MR_SECTION_NOT_FOUND");
                        continue;
                    }

                    line.setHeader(header);
                    line.setMeteringPoint(mrLine.getMeteringPoint());
                    line.setSection(section);
                    line.setBypassMeteringPoint(t.getBypassMeteringPoint());
                    line.setBypassMode(t.getBypassMode());
                    line.setIsBypassSection(t.getIsBypassSection());
                    line.setIsIgnore(false);
                    line.setParam(inverseParam(t.getParam(), mrLine.getIsInverse()));
                    line.setUnit(t.getUnit());
                    line.setMeter(t.getMeter());
                    line.setMeterHistory(t.getMeterHistory());
                    line.setStartMeteringDate(t.getStartMeteringDate());
                    line.setEndMeteringDate(t.getEndMeteringDate());
                    line.setStartVal(t.getStartVal());
                    line.setEndVal(t.getEndVal());
                    line.setDelta(t.getDelta());
                    line.setMeterRate(t.getMeterRate());
                    line.setVal(t.getVal());
                    line.setUnderCountVal(t.getUnderCountVal());
                    line.setUndercount(t.getUnderCount());
                    resultLines.add(line);
                }
            }

            saveLines(resultLines);
            updateStatus(header, BatchStatusEnum.C);

            logger.info("Metering reading for header " + header.getId() + " completed");
            return true;
        }

        catch (Exception e) {
            messageService.addMessage(header, null,  docCode,"RUNTIME_EXCEPTION");
            updateStatus(header, BatchStatusEnum.E);
            logger.error("Metering reading for header " + header.getId() + " terminated with exception: " + e.toString() + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void saveLines(List<BalanceSubstResultMrLine> resultLines) {
        balanceSubstResultMrLineRepo.save(resultLines);
        balanceSubstResultMrLineRepo.flush();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void deleteLines(BalanceSubstResultHeader header) {
        List<BalanceSubstResultMrLine> lines = balanceSubstResultMrLineRepo.findAllByHeaderId(header.getId());
        for (int i=0; i<lines.size(); i++)
            balanceSubstResultMrLineRepo.delete(lines.get(i));
        balanceSubstResultMrLineRepo.flush();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void deleteMessages(BalanceSubstResultHeader header, String docCode) {
        messageService.deleteMessages(header, docCode);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void updateStatus(BalanceSubstResultHeader header, BatchStatusEnum status) {
        header.setStatus(status);
        balanceSubstResultHeaderRepo.save(header);
    }

    private String getSection(BalanceSubstMrLine mrLine) {
        if (mrLine.getIsSection1()) return "1";
        if (mrLine.getIsSection2()) return "2";
        if (mrLine.getIsSection3()) return "3";
        if (mrLine.getIsSection4()) return "4";
        if (mrLine.getIsSection5()) return "5";
        return "";
    }

    private Parameter inverseParam(Parameter param, Boolean isInverse) {
        if (isInverse) {
            if (param.getCode().equals("A+")) return mapParams.get("A-");
            if (param.getCode().equals("A-")) return mapParams.get("A+");
            if (param.getCode().equals("R+")) return mapParams.get("R-");
            if (param.getCode().equals("R-")) return mapParams.get("R+");
        }
        return param;
    }
}
