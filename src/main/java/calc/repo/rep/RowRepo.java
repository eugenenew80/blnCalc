package calc.repo.rep;

import calc.entity.rep.ReportRow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RowRepo extends JpaRepository<ReportRow, Long> { }
