package calc.repo.calc;

import calc.entity.calc.svr.SvrResultPart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SvrPartRepo extends JpaRepository<SvrResultPart, Long> {
    List<SvrResultPart> findAllByHeaderId(Long headerId);
}
