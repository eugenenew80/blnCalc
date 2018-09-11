package calc.repo.calc;

import calc.entity.calc.BsResultMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BsResultMessageRepo extends JpaRepository<BsResultMessage, Long> {
    List<BsResultMessage> findAllByHeaderId(Long headerId);
}
