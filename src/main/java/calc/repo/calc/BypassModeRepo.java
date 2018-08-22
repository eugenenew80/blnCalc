package calc.repo.calc;

import calc.entity.calc.BypassMode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BypassModeRepo extends JpaRepository<BypassMode, Long> {

    @Query("select m from BypassMode m where m.meteringPoint.id = ?1 and (m.startDate is null or m.startDate <= ?2) and (m.endDate is null or m.endDate > ?2) order by m.startDate, m.endDate" )
    List<BypassMode> findAllByMeteringPointIdAndDate(Long meteringPointId, LocalDateTime dateTime);
}
