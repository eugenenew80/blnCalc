package calc.repo;

import calc.entity.Value;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;

@Repository
public interface ValueRepo extends JpaRepository<Value, Long> {
    Value findAllByMeteringPointIdAndStartDateAndEndDate(
        Long meteringPointId,
        LocalDate startDateTime,
        LocalDate endDateTime
    );
}
