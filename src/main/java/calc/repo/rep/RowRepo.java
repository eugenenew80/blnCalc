package calc.repo.rep;

import calc.entity.rep.TableRow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RowRepo extends JpaRepository<TableRow, Long> {
    List<TableRow> findAllByDivisionId(Long divisionId);
    List<TableRow> findAllByDivisionIdAndSectionId(Long divisionId, Long sectionId);
}
