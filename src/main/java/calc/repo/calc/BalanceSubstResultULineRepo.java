package calc.repo.calc;

import calc.entity.calc.BalanceSubstResultULine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BalanceSubstResultULineRepo extends JpaRepository<BalanceSubstResultULine, Long> { }
