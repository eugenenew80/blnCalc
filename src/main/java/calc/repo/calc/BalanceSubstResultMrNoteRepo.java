package calc.repo.calc;

import calc.entity.calc.bs.mr.BalanceSubstResultMrNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BalanceSubstResultMrNoteRepo extends JpaRepository<BalanceSubstResultMrNote, Long> {
    List<BalanceSubstResultMrNote> findAllByHeaderId(Long headerId);
}
