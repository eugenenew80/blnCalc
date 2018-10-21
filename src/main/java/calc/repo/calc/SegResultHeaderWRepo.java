package calc.repo.calc;

import calc.entity.calc.enums.BatchStatusEnum;
import calc.entity.calc.seg.SegResultHeaderW;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SegResultHeaderWRepo extends JpaRepository<SegResultHeaderW, Long> {
    List<SegResultHeaderW> findAllByStatus(BatchStatusEnum status);
}
