package calc.repo.calc;

import calc.entity.calc.BalanceSubstResultHeader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BalanceSubstResultHeaderRepo extends JpaRepository<BalanceSubstResultHeader, Long> { }
