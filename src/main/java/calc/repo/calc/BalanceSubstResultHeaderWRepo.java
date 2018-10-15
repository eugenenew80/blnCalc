package calc.repo.calc;

import calc.entity.calc.bs.BalanceSubstResultHeaderW;
import calc.entity.calc.enums.BatchStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BalanceSubstResultHeaderWRepo extends JpaRepository<BalanceSubstResultHeaderW, Long> {
    List<BalanceSubstResultHeaderW> findAllByStatus(BatchStatusEnum status);
}
