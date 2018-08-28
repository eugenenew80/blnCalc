package calc.formula.service.impl;

import calc.entity.calc.BalanceSubstResultMrLine;
import calc.entity.calc.MeteringPoint;
import calc.formula.service.BsResultMrService;
import calc.repo.calc.BalanceSubstResultMrLineRepo;
import calc.repo.calc.BalanceSubstResultULineRepo;
import calc.repo.calc.MeteringPointRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BsResultMtServiceImpl implements BsResultMrService {
    private final MeteringPointRepo meteringPointRepo;
    private final BalanceSubstResultMrLineRepo balanceSubstResultMrLineRepo;

    @Override
    public List<BalanceSubstResultMrLine> getValues(Long headerId, String meteringPointCode) {
        MeteringPoint meteringPoint = meteringPointRepo.findByCode(meteringPointCode);
        return balanceSubstResultMrLineRepo.findAllByHeaderIdAndMeteringPointId(headerId, meteringPoint.getId());
    }
}
