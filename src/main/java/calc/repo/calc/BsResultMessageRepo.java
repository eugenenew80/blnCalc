package calc.repo.calc;

import calc.entity.calc.bs.BalanceSubstResultMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BsResultMessageRepo extends JpaRepository<BalanceSubstResultMessage, Long> {
    List<BalanceSubstResultMessage> findAllByHeaderId(Long headerId);
}
