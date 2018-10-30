package calc.repo.calc;

import calc.entity.calc.loss.LossFactResultSec1Line;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LossFactResultSec1LineRepo extends JpaRepository<LossFactResultSec1Line, Long> {
    List<LossFactResultSec1Line> findAllByHeaderId(Long headerId);
}
