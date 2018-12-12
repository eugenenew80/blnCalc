package calc.formula.service.impl;

import calc.entity.calc.MeteringPoint;
import calc.entity.calc.bs.pe.ReactorValue;
import calc.formula.service.ReactorValueService;
import calc.repo.calc.MeteringPointRepo;
import calc.repo.calc.ReactorValueRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReactorValueServiceImpl implements ReactorValueService {
    private final MeteringPointRepo meteringPointRepo;
    private final ReactorValueRepo reactorValueRepo;

    @Override
    public List<ReactorValue> getValues(Long headerId, String meteringPointCode) {
        MeteringPoint meteringPoint = meteringPointRepo.findByCode(meteringPointCode);
        return reactorValueRepo.findAllByHeaderIdAndMeteringPointOutId(headerId, meteringPoint.getId());
    }
}
