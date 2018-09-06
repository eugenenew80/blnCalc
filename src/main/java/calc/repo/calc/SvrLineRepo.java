package calc.repo.calc;

import calc.entity.calc.SvrLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SvrLineRepo extends JpaRepository<SvrLine, Long> {
    List<SvrLine> findAllByHeaderId(Long headerId);

    List<SvrLine> findAllByHeaderIdAndMeteringPointId(Long headerId, Long meteringPointId);
}
