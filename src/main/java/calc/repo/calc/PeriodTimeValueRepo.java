package calc.repo.calc;

import calc.entity.calc.PeriodTimeValue;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PeriodTimeValueRepo extends JpaRepository<PeriodTimeValue, Long> {
    @EntityGraph(value = "PeriodTimeValue.allJoins" , type= EntityGraph.EntityGraphType.FETCH)
    @Query("select t from PeriodTimeValue t where t.meteringPoint.code = ?1 and t.param.code = ?2 and t.meteringDate between ?3 and ?4")
    List<PeriodTimeValue> findByParam(String mpCode, String param, LocalDateTime startDateTime, LocalDateTime endDateTime);
}
