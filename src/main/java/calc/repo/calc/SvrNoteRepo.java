package calc.repo.calc;

import calc.entity.calc.svr.SvrNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SvrNoteRepo extends JpaRepository<SvrNote, Long> {
    List<SvrNote> findAllByHeaderId(Long headerId);
}
