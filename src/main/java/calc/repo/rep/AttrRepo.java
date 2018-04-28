package calc.repo.rep;

import calc.entity.rep.ReportAttr;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AttrRepo extends JpaRepository<ReportAttr, Long> { }
