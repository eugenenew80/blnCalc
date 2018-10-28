package calc.repo.calc;

import calc.entity.calc.bs.mr.BalanceSubstResultBpLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BalanceSubstResultBpLineRepo extends JpaRepository<BalanceSubstResultBpLine, Long> {
    List<BalanceSubstResultBpLine> findAllByHeaderId(Long headerId);

    List<BalanceSubstResultBpLine> findAllByHeaderIdAndMeteringPointId(Long headerId, Long meteringPointId);
}
