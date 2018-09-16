package calc.repo.calc;

import calc.entity.calc.bs.u.BalanceSubstResultULine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BalanceSubstResultULineRepo extends JpaRepository<BalanceSubstResultULine, Long> {
    List<BalanceSubstResultULine> findAllByHeaderId(Long headerId);

    List<BalanceSubstResultULine> findAllByHeaderIdAndMeteringPointId(Long headerId, Long meteringPointId);
}
