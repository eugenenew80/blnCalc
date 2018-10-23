package calc.repo.calc;

import calc.entity.calc.enums.BatchStatusEnum;
import calc.entity.calc.inter.InterResultHeaderW;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface InterResultHeaderWRepo extends JpaRepository<InterResultHeaderW, Long> {
    List<InterResultHeaderW> findAllByStatus(BatchStatusEnum status);
}
