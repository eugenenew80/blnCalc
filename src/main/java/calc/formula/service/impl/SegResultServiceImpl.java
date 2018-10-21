package calc.formula.service.impl;

import calc.entity.calc.seg.SegResultLine;
import calc.formula.service.SegResultService;
import calc.repo.calc.SegResultLineRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SegResultServiceImpl implements SegResultService {
    private final SegResultLineRepo segResultLineRepo;

    @Override
    public List<SegResultLine> getValues(Long headerId, String mpCode) {
        return segResultLineRepo.findByMeteringPoint(headerId, mpCode);
    }

    @Override
    public List<SegResultLine> getValues(Long headerId, String mpCode, String paramCode) {
        return segResultLineRepo.findByParam(headerId, mpCode, paramCode);
    }
}
