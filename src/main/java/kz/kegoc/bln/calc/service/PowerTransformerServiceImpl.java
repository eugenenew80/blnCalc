package kz.kegoc.bln.calc.service;

import kz.kegoc.bln.calc.CalcContext;
import kz.kegoc.bln.entity.PowerTransformer;
import kz.kegoc.bln.repo.PowerTransformerRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
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
