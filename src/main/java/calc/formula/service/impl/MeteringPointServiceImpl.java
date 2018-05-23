package calc.formula.service.impl;

import calc.entity.calc.MeteringPoint;
import calc.entity.calc.MeteringPointMeter;
import calc.formula.CalcContext;
import calc.formula.service.MeteringPointService;
import calc.repo.calc.MeteringPointRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MeteringPointServiceImpl implements MeteringPointService {
    private final MeteringPointRepo repo;

    @Override
    public Double getDoubleAttribute(Long id, String code, String attr, CalcContext context) {
        return null;
    }

    @Override
    public String getStringAttribute(Long id, String code, String attr, CalcContext context) {
        MeteringPoint meteringPoint = null;
        if (id!=null)
            meteringPoint = repo.findOne(id);
        else if (code!=null)
            meteringPoint = repo.findByCode(code);

        String value = null;
        if (attr.equals("code"))
            value = meteringPoint.getCode();

        if (attr.equals("name"))
            value = meteringPoint.getName();

        if (attr.equals("serial")) {
            List<MeteringPointMeter> meters = meteringPoint.getMeters();
            if (!meters.isEmpty())
                value = meters.get(0).getMeter().getSerialNumber();
        }

        return value;
    }
}
