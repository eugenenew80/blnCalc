package kz.kegoc.bln.calc.service;

import kz.kegoc.bln.calc.CalcContext;
import kz.kegoc.bln.entity.PowerLine;
import kz.kegoc.bln.repo.PowerLineRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PowerLineServiceImpl implements PowerLineService {

    @Autowired
    private PowerLineRepo repo;

    @Override
    public Double getNumberAttribute(Long id, String code, String attr, CalcContext context) {
        PowerLine powerLine;
        if (id!=null)
            powerLine = repo.findOne(id);
        else if (code!=null)
            powerLine = repo.findByCode(code);
        else
            powerLine = null;

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
