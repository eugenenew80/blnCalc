package calc.repo.calc;

import calc.entity.calc.loss.LossFactResultSec2Line;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LossFactResultSec2LineRepo extends JpaRepository<LossFactResultSec2Line, Long> {
    List<LossFactResultSec2Line> findAllByHeaderId(Long headerId);
}
