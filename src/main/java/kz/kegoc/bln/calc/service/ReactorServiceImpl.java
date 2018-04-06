package kz.kegoc.bln.calc.service;

import kz.kegoc.bln.calc.CalcContext;
import kz.kegoc.bln.entity.Reactor;
import kz.kegoc.bln.repo.ReactorRepo;
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
