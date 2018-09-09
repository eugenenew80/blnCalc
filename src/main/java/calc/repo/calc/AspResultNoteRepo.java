package calc.repo.calc;

import calc.entity.calc.AspResultNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AspResultNoteRepo extends JpaRepository<AspResultNote, Long> {
    List<AspResultNote> findAllByHeaderId(Long headerId);
}
