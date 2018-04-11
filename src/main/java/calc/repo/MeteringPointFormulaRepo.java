package calc.repo;

import calc.entity.MeteringPointFormula;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MeteringPointFormulaRepo extends JpaRepository<MeteringPointFormula, Long> {
    List<MeteringPointFormula> findAllByMeteringPointId(Long id);
}
