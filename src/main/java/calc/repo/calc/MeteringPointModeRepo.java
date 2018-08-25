package calc.repo.calc;

import calc.entity.calc.MeterHistory;
import calc.entity.calc.MeteringPointMode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MeteringPointModeRepo extends JpaRepository<MeteringPointMode, Long> {
    @Query("select t from MeteringPointMode t where t.meteringPoint.id = ?1 and (((t.startDate is null or t.startDate <= ?2) and (t.endDate is null or t.endDate > ?3)) or t.startDate between ?2 and ?3 or t.endDate between ?2 and ?3) order by t.startDate, t.endDate" )
    List<MeteringPointMode> findAllByMeteringPointIdAndDate(Long meteringPointId, LocalDateTime startDate, LocalDateTime endDateTime);

}
