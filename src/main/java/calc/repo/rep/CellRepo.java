package calc.repo.rep;

import calc.entity.rep.TableCell;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CellRepo extends JpaRepository<TableCell, Long> { }
