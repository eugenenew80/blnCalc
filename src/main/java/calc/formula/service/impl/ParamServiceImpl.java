package calc.formula.service.impl;

import calc.entity.calc.Parameter;
import calc.formula.service.ParamService;
import calc.repo.calc.ParameterRepo;
import lombok.RequiredArgsConstructor;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashMap;
import java.util.Map;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ParamServiceImpl implements ParamService {
    private final ParameterRepo parameterRepo;
    private final CacheManager ehcacheManager;

    @Override
    public Map<String, Parameter> getValues() {
        Map<String, Parameter> mapParams = new HashMap<>();
        for (Parameter param : parameterRepo.findAll())
            mapParams.put(param.getCode(), param);

        return mapParams;
    }

    @Override
    public Parameter getParam(String paramCode) {
        Cache<String, Parameter> paramCache = ehcacheManager.getCache("paramCache", String.class, Parameter.class);
        Parameter param = paramCache.get(paramCode);
        if (param == null) {
            param = parameterRepo.findByCode(paramCode);
            paramCache.putIfAbsent(paramCode, param);
        }
        return param;
    }
}
