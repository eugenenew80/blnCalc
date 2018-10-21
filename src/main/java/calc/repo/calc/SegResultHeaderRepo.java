package calc.repo.calc;

import calc.entity.calc.enums.BatchStatusEnum;
import calc.entity.calc.seg.SegResultHeader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SegResultHeaderRepo extends JpaRepository<SegResultHeader, Long> {
    List<SegResultHeader> findAllByStatus(BatchStatusEnum status);
}
