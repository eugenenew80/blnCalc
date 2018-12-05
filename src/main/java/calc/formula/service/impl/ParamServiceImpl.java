package calc.formula.service.impl;

import calc.entity.calc.Parameter;
import calc.formula.service.ParamService;
import calc.repo.calc.ParameterRepo;
import lombok.RequiredArgsConstructor;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.PostConstruct;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ParamServiceImpl implements ParamService {
    private final ParameterRepo parameterRepo;
    private final CacheManager ehcacheManager;
    private Cache<String, Parameter> paramCache = null;

    @PostConstruct
    public void init() {
        paramCache = ehcacheManager.getCache("paramCache", String.class, Parameter.class);
    }

    @Override
    public Parameter getParam(String paramCode) {
        Parameter param = paramCache.get(paramCode);
        if (param == null) {
            param = parameterRepo.findByCode(paramCode);
            paramCache.putIfAbsent(paramCode, param);
        }
        return param;
    }
}
