package calc.repo.calc;

import calc.entity.calc.seg.SegResultMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SegResultMessageRepo extends JpaRepository<SegResultMessage, Long> {
    List<SegResultMessage> findAllByHeaderId(Long headerId);
}
