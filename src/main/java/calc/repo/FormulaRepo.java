package calc.repo;

import calc.entity.Formula;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FormulaRepo extends JpaRepository<Formula, Long> {
    @EntityGraph(value = "Formula.allJoins" , type= EntityGraph.EntityGraphType.LOAD)
    Formula findByCode(String code);

    @EntityGraph(value = "Formula.allJoins" , type= EntityGraph.EntityGraphType.FETCH)
    List<Formula> findAllByMeteringPointOrgId(Long orgId);
}
