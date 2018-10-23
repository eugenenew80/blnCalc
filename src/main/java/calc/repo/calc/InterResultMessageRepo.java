package calc.repo.calc;

import calc.entity.calc.inter.InterResultMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface InterResultMessageRepo extends JpaRepository<InterResultMessage, Long> {
    List<InterResultMessage> findAllByHeaderId(Long headerId);
}
