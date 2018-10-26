package calc.repo.calc;

import calc.entity.calc.AtTimeValue;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AtTimeValueRepo extends JpaRepository<AtTimeValue, Long> {
    @EntityGraph(value = "AtTimeValue.allJoins" , type= EntityGraph.EntityGraphType.FETCH)
    @Query("select t from AtTimeValue t where t.meteringPoint.code = ?1 and t.param.code = ?2 and t.meteringDate = ?3")
    List<AtTimeValue> findByParam(String mpCode, String param, LocalDateTime meteringDate);
}
