package calc.formula.service.impl;

import calc.formula.CalcContext;
import calc.entity.PowerTransformer;
import calc.formula.service.PowerTransformerService;
import calc.repo.PowerTransformerRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PowerTransformerServiceImpl implements PowerTransformerService {

    @Autowired
    private PowerTransformerRepo repo;

    @Override
    public Double getNumberAttribute(Long id, String attr, CalcContext context) {
        PowerTransformer powerTransformer = repo.findOne(id);
        if (powerTransformer == null)
            return null;

        Double value = null;
        if (attr.equals("delta_pxx"))
            value = powerTransformer.getDeltaPxx();

        if (attr.equals("snom"))
            value = powerTransformer.getSnom();

        if (attr.equals("unom_h"))
            value = powerTransformer.getUnomH();

        if (attr.equals("pkz_hl"))
            value = powerTransformer.getPkzHL();

        if (attr.equals("pkz_hm"))
            value = powerTransformer.getPkzHM();

        if (attr.equals("pkz_ml"))
            value = powerTransformer.getPkzML();

        return value;
    }
}
