package calc.repo.calc;

import calc.entity.calc.UnderCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UnderCountRepo extends JpaRepository<UnderCount, Long> {

    @Query("select t from UnderCount t where t.meteringPoint.id = ?1 and ( (t.startDate is null or t.startDate <= ?2) and (t.endDate is null or t.endDate > ?3) or t.startDate between ?2 and ?3 or t.endDate between ?2 and ?3) order by t.startDate, t.endDate" )
    List<UnderCount> findAllByMeteringPointIdAndDate(Long meteringPointId, LocalDateTime startDate, LocalDateTime endDateTime);
}
