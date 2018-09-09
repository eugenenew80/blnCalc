package calc.formula.calculation;

import calc.entity.calc.*;
import calc.entity.calc.enums.BatchStatusEnum;
import calc.entity.calc.enums.LangEnum;
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
import java.util.*;

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
            for (BalanceSubstLine line : header.getHeader().getLines()) {
                if (line.getParam() == null) {
                    Map<String, String> sections = getSections(line);
                    for (String section : sections.keySet()) {
                        String param = line.getParam() == null ? sections.get(section) : line.getParam().getCode();
                        param = inverseParam(param, line.getIsInverse());
                        MeteringPoint meteringPoint = line.getMeteringPoint();
                        Double val = getMrVal(line, param, context);

                        BalanceSubstResultLine resultLine = new BalanceSubstResultLine();
                        resultLine.setHeader(header);
                        resultLine.setMeteringPoint(meteringPoint);
                        resultLine.setParam(mapParams.get(param));
                        resultLine.setRate(Optional.ofNullable(line.getRate()).orElse(1d));
                        resultLine.setSection(section);
                        resultLine.setVal(val);
                        if (meteringPoint!=null && meteringPoint.getVoltageClass()!=null)
                            resultLine.setSubSection(meteringPoint.getVoltageClass().getValue().toString());

                        for (BalanceSubstLineTranslate lineTranslate : line.getTranslates()) {
                            BalanceSubstResultLineTranslate resultLineTranslate = new BalanceSubstResultLineTranslate();
                            resultLineTranslate.setLang(LangEnum.RU);
                            resultLineTranslate.setLine(resultLine);
                            resultLineTranslate.setName(lineTranslate.getName());
                            resultLine.getTranslates().add(resultLineTranslate);
                        }
                        resultLines.add(resultLine);
                    }
                }
            }

            balanceSubstResultLineRepo.save(resultLines);
            updateStatus(header, BatchStatusEnum.C);
            return true;
        }

        catch (Exception e) {
            updateStatus(header, BatchStatusEnum.E);
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
        if (bLine.getIsSection2()) map.put("2", "A-");
        if (bLine.getIsSection3()) map.put("3", "A-");
        if (bLine.getIsSection4()) map.put("4", "A-");
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
