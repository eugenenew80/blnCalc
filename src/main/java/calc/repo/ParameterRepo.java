package calc.repo;

import calc.entity.Parameter;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParameterRepo extends JpaRepository<Parameter, Long> {
    @Cacheable(value="paramsCache", key="{#code, #paramType}" )
    Parameter findByCodeAndParamType(String code, String paramType);
}
