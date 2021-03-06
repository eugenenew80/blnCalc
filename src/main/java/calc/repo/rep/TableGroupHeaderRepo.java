package calc.repo.rep;

import calc.entity.rep.TableGroupHeader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TableGroupHeaderRepo extends JpaRepository<TableGroupHeader, Long> { }
