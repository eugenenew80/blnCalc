package calc.repo.calc;

import calc.entity.calc.inter.InterResultNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface InterResultNoteRepo extends JpaRepository<InterResultNote, Long> {
    List<InterResultNote> findAllByHeaderId(Long headerId);
}
