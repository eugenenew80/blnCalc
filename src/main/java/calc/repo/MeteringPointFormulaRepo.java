package calc.repo;

import calc.entity.MeteringPointFormula;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MeteringPointFormulaRepo extends JpaRepository<MeteringPointFormula, Long> { }
