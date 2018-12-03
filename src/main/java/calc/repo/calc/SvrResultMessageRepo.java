package calc.repo.calc;

import calc.entity.calc.svr.SvrResultMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SvrResultMessageRepo extends JpaRepository<SvrResultMessage, Long> {
    List<SvrResultMessage> findAllByHeaderId(Long headerId);
}
