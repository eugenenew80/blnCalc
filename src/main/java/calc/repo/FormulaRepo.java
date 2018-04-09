package calc.repo;

import calc.entity.Formula;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FormulaRepo extends JpaRepository<Formula, Long> {
    Formula findByCode(String code);
}
