package calc.repo.calc;

import calc.entity.calc.AspResultMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AspResultMessageRepo extends JpaRepository<AspResultMessage, Long> {
    List<AspResultMessage> findAllByHeaderId(Long headerId);
}
