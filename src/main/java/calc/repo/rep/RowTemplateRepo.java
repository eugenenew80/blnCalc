package calc.repo.rep;

import calc.entity.rep.RowTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RowTemplateRepo extends JpaRepository<RowTemplate, Long> {
}
