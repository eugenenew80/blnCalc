package calc.formula.service.impl;

import calc.entity.calc.distr.DistrResultHeader;
import calc.entity.calc.distr.DistrResultLine;
import calc.formula.CalcContext;
import calc.formula.service.DistributionService;
import calc.repo.calc.DistrResultHeaderRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import static java.util.stream.Collectors.toList;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DistributionServiceImpl implements DistributionService {
    private final DistrResultHeaderRepo distrResultHeaderRepo;

    @Override
    public List<DistrResultLine> getValues(String meteringPointCode, String parameterCode, CalcContext context) {
        List<DistrResultHeader> distributionList = distrResultHeaderRepo.findByOrg(
            context.getOrgId(),
            context.getDataType().name(),
            context.getStartDate(),
            context.getEndDate()
        );

        return distributionList.stream()
            .flatMap(t -> t.getLines().stream())
            .filter(t -> t.getMeteringPoint() != null)
            .filter(t -> t.getParam() != null)
            .filter(t -> t.getMeteringPoint().getCode().equals(meteringPointCode))
            .filter(t -> t.getParam().getCode().equals(parameterCode))
            .collect(toList());
    }
}
