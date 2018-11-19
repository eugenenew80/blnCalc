package calc.repo.calc;

import calc.entity.calc.reg.RegResultMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RegResultMessageRepo extends JpaRepository<RegResultMessage, Long> {
    List<RegResultMessage> findAllByHeaderId(Long headerId);
}
