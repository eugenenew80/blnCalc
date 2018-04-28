package calc.repo.rep;

import calc.entity.rep.ReportSheet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SheetRepo extends JpaRepository<ReportSheet, Long> { }
