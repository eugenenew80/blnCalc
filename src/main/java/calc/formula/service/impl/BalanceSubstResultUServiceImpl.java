package calc.formula.service.impl;

import calc.entity.calc.bs.u.BalanceSubstResultULine;
import calc.entity.calc.MeteringPoint;
import calc.formula.service.BalanceSubstResultUService;
import calc.repo.calc.BalanceSubstResultULineRepo;
import calc.repo.calc.MeteringPointRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BalanceSubstResultUServiceImpl implements BalanceSubstResultUService {
    private final MeteringPointRepo meteringPointRepo;
    private final BalanceSubstResultULineRepo balanceSubstResultULineRepo;

    @Override
    public List<BalanceSubstResultULine> getValues(Long headerId, String meteringPointCode) {
        MeteringPoint meteringPoint = meteringPointRepo.findByCode(meteringPointCode);
        return balanceSubstResultULineRepo.findAllByHeaderIdAndMeteringPointId(headerId, meteringPoint.getId());
    }
}
