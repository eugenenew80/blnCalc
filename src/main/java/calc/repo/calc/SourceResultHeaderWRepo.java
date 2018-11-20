package calc.repo.calc;

import calc.entity.calc.enums.BatchStatusEnum;
import calc.entity.calc.source.SourceResultHeaderW;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SourceResultHeaderWRepo extends JpaRepository<SourceResultHeaderW, Long> {
    List<SourceResultHeaderW> findAllByStatus(BatchStatusEnum status);
}
