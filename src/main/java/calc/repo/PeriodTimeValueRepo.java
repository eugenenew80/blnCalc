package calc.repo;

import calc.entity.PeriodTimeValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PeriodTimeValueRepo extends JpaRepository<PeriodTimeValue, Long> {
    List<PeriodTimeValue> findAllByMeteringPointIdAndParamIdAndMeteringDateBetween(
        Long meteringPointId,
        Long paramId,
        LocalDateTime startDateTime,
        LocalDateTime endDateTime
    );
}
