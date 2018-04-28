package calc.repo.rep;

import calc.entity.rep.ReportTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TableRepo extends JpaRepository<ReportTable, Long> { }
