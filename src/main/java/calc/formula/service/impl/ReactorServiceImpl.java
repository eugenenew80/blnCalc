package calc.formula.service.impl;

import calc.formula.CalcContext;
import calc.entity.calc.Reactor;
import calc.formula.service.ReactorService;
import calc.repo.calc.ReactorRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReactorServiceImpl implements ReactorService {

    @Autowired
    private ReactorRepo repo;

    @Override
    public Double getNumberAttribute(Long id, String attr, CalcContext context) {
        Reactor reactor = repo.findOne(id);
        if (reactor == null)
            return null;

        Double value = null;
        if (attr.equals("delta_pr"))
            value = reactor.getDeltaPr();

        if (attr.equals("unom"))
            value = reactor.getUnom();

        return value;
    }
}
