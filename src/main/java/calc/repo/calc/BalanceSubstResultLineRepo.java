package calc.repo.calc;

import calc.entity.calc.BalanceSubstResultLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BalanceSubstResultLineRepo extends JpaRepository<BalanceSubstResultLine, Long> {
    List<BalanceSubstResultLine> findAllByHeaderId(Long headerId);

    List<BalanceSubstResultLine> findAllByHeaderIdAndMeteringPointId(Long headerId, Long meteringPointId);
}
