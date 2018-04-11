package calc.repo;

import calc.entity.MeteringPointFormula;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MeteringPointFormulaRepo extends JpaRepository<MeteringPointFormula, Long> {

    @EntityGraph(value = "MeteringPointFormula.allJoins" , type= EntityGraph.EntityGraphType.FETCH)
    List<MeteringPointFormula> findAll();
}
