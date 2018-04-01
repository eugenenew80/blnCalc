package kz.kegoc.bln.repo;

import kz.kegoc.bln.entity.PeriodTimeValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PeriodTimeValueRepo extends JpaRepository<PeriodTimeValue, Long> {
    List<PeriodTimeValue> findAllByMeteringPointIdAndParamIdAndMeteringDateBetween(
        Long meteringPointId,
        Long paramId,
        LocalDateTime start,
        LocalDateTime end
    );
}
