package calc.repo.calc;

import calc.entity.calc.MeterHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MeterHistoryRepo extends JpaRepository<MeterHistory, Long> {

    @Query("select h from MeterHistory h where h.meteringPoint.id = ?1 and (h.startDate is null or h.startDate <= ?2) and (h.endDate is null or h.endDate > ?2) order by h.startDate, h.endDate" )
    List<MeterHistory> findAllByMeteringPointIdAndDate(Long meteringPointId, LocalDateTime dateTime);
}
