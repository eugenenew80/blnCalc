package calc.formula.service.impl;

import calc.entity.calc.enums.LangEnum;
import calc.formula.CalcContext;
import calc.entity.calc.PowerTransformer;
import calc.formula.service.PowerTransformerService;
import calc.repo.calc.PowerTransformerRepo;
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
    public Double getDoubleAttribute(Long id, String attr, CalcContext context) {
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


        if (attr.equals("resist") && powerTransformer.getWindingsNumber() != null && powerTransformer.getWindingsNumber().equals(3l) ) {
            Double pkzHL = powerTransformer.getPkzHL();
            Double uNomH = powerTransformer.getUnomH();
            Double sNom = powerTransformer.getSnom();
            value = pkzHL * (Math.pow(uNomH, 2) / Math.pow(sNom, 2));
        }

        return value;
    }

    @Override
    public String getStringAttribute(Long id, String attr, CalcContext context) {
        PowerTransformer powerTransformer = repo.findOne(id);
        if (powerTransformer == null)
            return null;

        String value = null;
        if (attr.equals("name") && powerTransformer.getTranslates().containsKey(context.getLang()))
            value = powerTransformer.getTranslates()
                .get(context.getLang())
                .getName();

        return value;
    }
}
