package calc.repo.calc;

import calc.entity.calc.MeterHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MeterHistoryRepo extends JpaRepository<MeterHistory, Long> {

    @Query("select t from MeterHistory t where t.meteringPoint.id = ?1 and (((t.startDate is null or t.startDate <= ?2) and (t.endDate is null or t.endDate > ?3)) or t.startDate between ?2 and ?3 or t.endDate between ?2 and ?3) order by t.startDate, t.endDate" )
    List<MeterHistory> findAllByMeteringPointIdAndDate(Long meteringPointId, LocalDateTime startDate, LocalDateTime endDateTime);
}
