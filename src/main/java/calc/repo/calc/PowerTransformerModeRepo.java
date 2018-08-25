package calc.repo.calc;

import calc.entity.calc.PowerTransformerMode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PowerTransformerModeRepo extends JpaRepository<PowerTransformerMode, Long> {
    @Query("select t from PowerTransformerMode t where t.transformer.id = ?1 and (((t.startDate is null or t.startDate <= ?2) and (t.endDate is null or t.endDate > ?3)) or t.startDate between ?2 and ?3 or t.endDate between ?2 and ?3) order by t.startDate, t.endDate" )
    List<PowerTransformerMode> findAllByTransformerIdAndDate(Long transformerId, LocalDateTime startDate, LocalDateTime endDateTime);

}
