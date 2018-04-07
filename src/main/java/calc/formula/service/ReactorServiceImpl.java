package calc.formula.service;

import calc.formula.CalcContext;
import calc.entity.Reactor;
import calc.repo.ReactorRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
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
