package calc.repo.calc;

import calc.entity.calc.enums.BatchStatusEnum;
import calc.entity.calc.reg.RegResultHeader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RegResultHeaderRepo extends JpaRepository<RegResultHeader, Long> {
    List<RegResultHeader> findAllByStatus(BatchStatusEnum status);
}
