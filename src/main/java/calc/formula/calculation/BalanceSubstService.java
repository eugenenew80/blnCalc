package calc.formula.calculation;

import calc.entity.calc.*;
import calc.entity.calc.enums.BatchStatusEnum;
import calc.formula.CalcContext;
import calc.formula.expression.impl.MeteringReadingExpression;
import calc.formula.service.BsResultMrService;
import calc.repo.calc.BalanceSubstResultHeaderRepo;
import calc.repo.calc.BalanceSubstResultLineRepo;
import calc.repo.calc.ParameterRepo;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("Duplicates")
@Service
@RequiredArgsConstructor
public class BalanceSubstService {
    private static final Logger logger = LoggerFactory.getLogger(BalanceSubstService.class);
    private final BalanceSubstResultHeaderRepo balanceSubstResultHeaderRepo;
    private final BalanceSubstResultLineRepo balanceSubstResultLineRepo;
    private final ParameterRepo parameterRepo;
    private final BsResultMrService mrService;
    private static final String docCode = "BALANCE";    private Map<String, Parameter> mapParams = null;

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
                .isMeteringReading(true)
                .trace(new HashMap<>())
                .values(new HashMap<>())
                .build();

            List<BalanceSubstResultLine> resultLines = new ArrayList<>();
            for (BalanceSubstLine bsLine : header.getHeader().getLines()) {
                if (bsLine.getParam() == null) {
                    Map<String, String> sections = getSections(bsLine);
                    for (String section : sections.keySet()) {
                        String param = bsLine.getParam() == null ? sections.get(section) : bsLine.getParam().getCode();
                        param = inverseParam(param, bsLine.getIsInverse());

                        Double val = getMrVal(bsLine, param, context);
                        BalanceSubstResultLine line = new BalanceSubstResultLine();
                        line.setHeader(header);
                        line.setMeteringPoint(bsLine.getMeteringPoint());
                        line.setParam(mapParams.get(param));
                        line.setRate(bsLine.getRate());
                        line.setSection(section);
                        line.setVal(val);
                        resultLines.add(line);
                    }
                }
            }

            balanceSubstResultLineRepo.save(resultLines);
            updateStatus(header, BatchStatusEnum.C);
            logger.info("Metering reading for header " + header.getId() + " completed");

            return true;
        }

        catch (Exception e) {
            updateStatus(header, BatchStatusEnum.E);
            logger.error("Metering reading for header " + header.getId() + " terminated with exception");
            logger.error(e.toString() + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private Double getMrVal(BalanceSubstLine bsLine, String param, CalcContext context) {
        return MeteringReadingExpression.builder()
            .meteringPointCode(bsLine.getMeteringPoint().getCode())
            .parameterCode(param)
            .rate(bsLine.getRate())
            .context(context)
            .service(mrService)
            .build()
            .doubleValue();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteLines(BalanceSubstResultHeader header) {
        List<BalanceSubstResultLine> lines = balanceSubstResultLineRepo.findAllByHeaderId(header.getId());
        for (int i=0; i<lines.size(); i++)
            balanceSubstResultLineRepo.delete(lines.get(i));
        balanceSubstResultLineRepo.flush();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateStatus(BalanceSubstResultHeader header, BatchStatusEnum status) {
        header.setStatus(status);
        balanceSubstResultHeaderRepo.save(header);
    }

    private Map<String, String> getSections(BalanceSubstLine bLine) {
        Map<String, String> map = new HashMap<>();
        if (bLine.getIsSection1()) map.put("1", "A+");
        if (bLine.getIsSection2()) map.put("1", "A-");
        if (bLine.getIsSection3()) map.put("1", "A-");
        if (bLine.getIsSection4()) map.put("1", "A-");
        return map;
    }

    private String inverseParam(String param, Boolean isInverse) {
        if (isInverse) {
            if (param.equals("A+")) return "A-";
            if (param.equals("A-")) return "A+";
            if (param.equals("R+")) return "R-";
            if (param.equals("R-")) return "R+";
        }
        return param;
    }
}
