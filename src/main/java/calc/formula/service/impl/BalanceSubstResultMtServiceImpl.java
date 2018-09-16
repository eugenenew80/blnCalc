package calc.formula.service.impl;

import calc.entity.calc.bs.mr.BalanceSubstResultMrLine;
import calc.entity.calc.MeteringPoint;
import calc.formula.service.BalanceSubstResultMrService;
import calc.repo.calc.BalanceSubstResultMrLineRepo;
import calc.repo.calc.MeteringPointRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BalanceSubstResultMtServiceImpl implements BalanceSubstResultMrService {
    private final MeteringPointRepo meteringPointRepo;
    private final BalanceSubstResultMrLineRepo balanceSubstResultMrLineRepo;

    @Override
    public List<BalanceSubstResultMrLine> getValues(Long headerId, String meteringPointCode) {
        MeteringPoint meteringPoint = meteringPointRepo.findByCode(meteringPointCode);
        return balanceSubstResultMrLineRepo.findAllByHeaderIdAndMeteringPointId(headerId, meteringPoint.getId());
    }
}
