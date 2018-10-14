package calc.formula.service.impl;

import calc.entity.calc.MeteringPoint;
import calc.entity.calc.bs.pe.PowerTransformerValue;
import calc.formula.service.TransformerValueService;
import calc.repo.calc.MeteringPointRepo;
import calc.repo.calc.PowerTransformerValueRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TransformerValueServiceImpl implements TransformerValueService {
    private final MeteringPointRepo meteringPointRepo;
    private final PowerTransformerValueRepo transformerValueRepo;

    @Override
    public List<PowerTransformerValue> getValues(Long headerId, String meteringPointCode) {
        MeteringPoint meteringPoint = meteringPointRepo.findByCode(meteringPointCode);
        return transformerValueRepo.findAllByHeaderIdAndMeteringPointOutId(headerId, meteringPoint.getId());
    }
}
