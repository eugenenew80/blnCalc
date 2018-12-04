package calc.repo.calc;

import calc.entity.calc.svr.SvrResultNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SvrNoteRepo extends JpaRepository<SvrResultNote, Long> {
    List<SvrResultNote> findAllByHeaderId(Long headerId);
}
