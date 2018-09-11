package calc.formula.service.impl;

import calc.entity.calc.asp.AspResultLine;
import calc.entity.calc.MeteringPoint;
import calc.formula.service.AspResultService;
import calc.repo.calc.AspResultLineRepo;
import calc.repo.calc.MeteringPointRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AspResultServiceImpl implements AspResultService {
    private final MeteringPointRepo meteringPointRepo;
    private final AspResultLineRepo aspResultLineRepo;

    @Override
    public List<AspResultLine> getValues(Long headerId, String meteringPointCode) {
        MeteringPoint meteringPoint = meteringPointRepo.findByCode(meteringPointCode);
        return aspResultLineRepo.findAllByHeaderIdAndMeteringPointId(headerId, meteringPoint.getId());
    }
}
