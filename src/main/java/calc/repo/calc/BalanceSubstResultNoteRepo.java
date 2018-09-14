package calc.repo.calc;

import calc.entity.calc.bs.BalanceSubstResultNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BalanceSubstResultNoteRepo extends JpaRepository<BalanceSubstResultNote, Long> {
    List<BalanceSubstResultNote> findAllByHeaderId(Long headerId);
}
