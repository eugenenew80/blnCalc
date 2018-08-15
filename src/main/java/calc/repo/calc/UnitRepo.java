package calc.repo.calc;

import calc.entity.calc.Unit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UnitRepo extends JpaRepository<Unit, Long> {
    Unit findByCode(String code);
}
