package calc.repo.calc;

import calc.entity.calc.seg.SegResultNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SegResultNoteRepo extends JpaRepository<SegResultNote, Long> {
    List<SegResultNote> findAllByHeaderId(Long headerId);
}
