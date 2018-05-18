package calc.repo.rep;

import calc.entity.rep.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepo extends JpaRepository<Report, Long> {
    Report findByCodeAndIsTemplateIsTrue(String code);
    Report findByCode(String code);
}
