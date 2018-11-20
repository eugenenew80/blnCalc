package calc.repo.calc;

import calc.entity.calc.enums.BatchStatusEnum;
import calc.entity.calc.source.SourceResultHeader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SourceResultHeaderRepo extends JpaRepository<SourceResultHeader, Long> {
    List<SourceResultHeader> findAllByStatus(BatchStatusEnum status);
}
