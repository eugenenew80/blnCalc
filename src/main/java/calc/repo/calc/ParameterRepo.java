package calc.repo.calc;

import calc.entity.calc.Parameter;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ParameterRepo extends JpaRepository<Parameter, Long> {
    Parameter findByCode(String code);
}
