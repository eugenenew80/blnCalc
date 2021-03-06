package calc.repo.calc;

import calc.entity.calc.bs.mr.BalanceSubstResultMrLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BalanceSubstResultMrLineRepo extends JpaRepository<BalanceSubstResultMrLine, Long> {
    List<BalanceSubstResultMrLine> findAllByHeaderId(Long headerId);

    List<BalanceSubstResultMrLine> findAllByHeaderIdAndMeteringPointId(Long headerId, Long meteringPointId);
}
