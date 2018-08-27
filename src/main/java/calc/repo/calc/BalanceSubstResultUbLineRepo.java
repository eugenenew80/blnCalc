package calc.repo.calc;

import calc.entity.calc.BalanceSubstResultUbLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BalanceSubstResultUbLineRepo extends JpaRepository<BalanceSubstResultUbLine, Long> {
    List<BalanceSubstResultUbLine> findAllByHeaderId(Long headerId);
}
