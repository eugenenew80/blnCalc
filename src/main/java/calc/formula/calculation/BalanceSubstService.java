package calc.formula.calculation;

import calc.entity.calc.*;
import calc.entity.calc.enums.BatchStatusEnum;
import calc.formula.CalcContext;
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

@Service
@RequiredArgsConstructor
public class BalanceSubstService {
    private static final Logger logger = LoggerFactory.getLogger(BalanceSubstService.class);
    private final BalanceSubstResultHeaderRepo balanceSubstResultHeaderRepo;
    private final BalanceSubstResultLineRepo balanceSubstResultLineRepo;
    private final ParameterRepo parameterRepo;
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
                .startDate(header.getStartDate())
                .endDate(header.getEndDate())
                .orgId(header.getOrganization().getId())
                .energyObjectType("SUBSTATION")
                .energyObjectId(header.getSubstation().getId())
                .trace(new HashMap<>())
                .values(new HashMap<>())
                .build();

            List<BalanceSubstResultLine> resultLines = new ArrayList<>();
            for (BalanceSubstLine bLine : header.getHeader().getLines()) {
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

    private String getSection(BalanceSubstLine bLine) {
        if (bLine.getIsSection1()) return "1";
        if (bLine.getIsSection2()) return "2";
        if (bLine.getIsSection3()) return "3";
        if (bLine.getIsSection4()) return "4";
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
