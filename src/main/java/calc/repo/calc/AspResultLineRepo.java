package calc.repo.calc;

import calc.entity.calc.AspResultLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AspResultLineRepo extends JpaRepository<AspResultLine, Long> {
    List<AspResultLine> findAllByHeaderId(Long headerId);

    List<AspResultLine> findAllByHeaderIdAndMeteringPointId(Long headerId, Long meteringPointId);
}
