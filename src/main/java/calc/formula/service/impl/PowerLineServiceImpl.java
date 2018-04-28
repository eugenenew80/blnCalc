package calc.formula.service.impl;

import calc.formula.CalcContext;
import calc.entity.calc.PowerLine;
import calc.formula.service.PowerLineService;
import calc.repo.calc.PowerLineRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PowerLineServiceImpl implements PowerLineService {

    @Autowired
    private PowerLineRepo repo;

    @Override
    public Double getNumberAttribute(Long id, String code, String attr, CalcContext context) {
        PowerLine powerLine = null;
        if (id!=null)
            powerLine = repo.findOne(id);
        else if (code!=null)
            powerLine = repo.findByCode(code);

        if (powerLine == null)
            return null;

        Double value = null;
        if (attr.equals("r"))
            value = powerLine.getR();

        if (attr.equals("po"))
            value = powerLine.getPo();

        if (attr.equals("length"))
            value = powerLine.getLength();

        return value;
    }
}
