package calc.repo;

import calc.entity.AtTimeValue;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AtTimeValueRepo extends JpaRepository<AtTimeValue, Long> {

    @EntityGraph(value = "AtTimeValue.allJoins" , type= EntityGraph.EntityGraphType.FETCH)
    List<AtTimeValue> findAllByMeteringPointIdAndParamIdAndMeteringDate(
        Long meteringPointId,
        Long paramId,
        LocalDateTime meteringDate
    );
}
