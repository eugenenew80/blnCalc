package calc.repo.rep;

import calc.entity.rep.TableDivision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DivisionRepo extends JpaRepository<TableDivision, Long> { }
