package calc.repo.calc;

import calc.entity.calc.source.SourceResultMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SourceResultMessageRepo extends JpaRepository<SourceResultMessage, Long> {
    List<SourceResultMessage> findAllByHeaderId(Long headerId);
}
