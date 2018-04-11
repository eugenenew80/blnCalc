package calc.repo;

import calc.entity.Value;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ValueRepo extends JpaRepository<Value, Long> {
    List<Value> findAllByMeteringPointIdAndStartDateAndEndDate(
        Long meteringPointId,
        LocalDateTime startDateTime,
        LocalDateTime endDateTime
    );
}
