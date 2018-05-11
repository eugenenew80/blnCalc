package calc.repo.rep;

import calc.entity.rep.SheetColumn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ColumnRepo extends JpaRepository<SheetColumn, Long> { }
