package calc.repo.calc;

import calc.entity.calc.bs.BalanceSubstHeader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BalanceSubstHeaderRepo extends JpaRepository<BalanceSubstHeader, Long> { }
