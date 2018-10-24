package calc.formula.service.impl;

import calc.entity.calc.inter.InterResultMrLine;
import calc.formula.service.InterResultMrService;
import calc.repo.calc.InterResultMrLineRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class InterResultMrServiceImpl implements InterResultMrService {
    private final InterResultMrLineRepo interResultMrLineRepo;

    @Override
    public List<InterResultMrLine> getValues(Long headerId, String meteringPointCode) {
        return interResultMrLineRepo.findByMeteringPoint(headerId, meteringPointCode);
    }
}
