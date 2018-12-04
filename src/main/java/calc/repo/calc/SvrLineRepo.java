package calc.repo.calc;

import calc.entity.calc.svr.SvrResultLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SvrLineRepo extends JpaRepository<SvrResultLine, Long> {
    List<SvrResultLine> findAllByHeaderId(Long headerId);

    List<SvrResultLine> findAllByHeaderIdAndMeteringPointId(Long headerId, Long meteringPointId);
}
