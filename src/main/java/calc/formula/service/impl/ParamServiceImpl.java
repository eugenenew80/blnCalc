package calc.formula.service.impl;

import calc.entity.calc.Parameter;
import calc.formula.service.ParamService;
import calc.repo.calc.ParameterRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ParamServiceImpl implements ParamService {
    private final ParameterRepo parameterRepo;

    @Override
    public Map<String, Parameter> getValues() {
        Map<String, Parameter> mapParams = new HashMap<>();
        mapParams.put("A-", parameterRepo.findByCode("A-"));
        mapParams.put("A+", parameterRepo.findByCode("A+"));
        mapParams.put("R-", parameterRepo.findByCode("R-"));
        mapParams.put("R+", parameterRepo.findByCode("R+"));
        mapParams.put("U",  parameterRepo.findByCode("U"));
        return mapParams;
    }
}
