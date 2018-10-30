package calc.repo.calc;

import calc.entity.calc.loss.LossFactResultHeader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LossFactResultHeaderRepo extends JpaRepository<LossFactResultHeader, Long> { }
