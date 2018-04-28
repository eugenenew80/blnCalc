package calc.repo.rep;

import calc.entity.rep.ReportCell;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CellRepo extends JpaRepository<ReportCell, Long> { }
