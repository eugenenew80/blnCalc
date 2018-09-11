package calc.repo.calc;

import calc.entity.calc.bs.BalanceSubstResultHeader;
import calc.entity.calc.enums.BatchStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BalanceSubstResultHeaderRepo extends JpaRepository<BalanceSubstResultHeader, Long> {
    List<BalanceSubstResultHeader> findAllByStatus(BatchStatusEnum status);
}
