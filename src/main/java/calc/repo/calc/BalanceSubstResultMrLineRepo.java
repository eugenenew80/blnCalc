package calc.repo.calc;

import calc.entity.calc.BalanceSubstResultMrLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BalanceSubstResultMrLineRepo extends JpaRepository<BalanceSubstResultMrLine, Long> { }
