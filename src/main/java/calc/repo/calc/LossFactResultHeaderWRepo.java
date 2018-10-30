package calc.repo.calc;

import calc.entity.calc.enums.BatchStatusEnum;
import calc.entity.calc.loss.LossFactResultHeaderW;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LossFactResultHeaderWRepo extends JpaRepository<LossFactResultHeaderW, Long> {
    List<LossFactResultHeaderW> findAllByStatus(BatchStatusEnum status);
}
