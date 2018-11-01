package calc.repo.calc;

import calc.entity.calc.loss.LossFactResultMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LossFactResultMessageRepo extends JpaRepository<LossFactResultMessage, Long> {
    List<LossFactResultMessage> findAllByHeaderId(Long headerId);
}
