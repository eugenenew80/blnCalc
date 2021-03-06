package calc.repo.calc;

import calc.entity.calc.enums.BatchStatusEnum;
import calc.entity.calc.reg.RegResultHeaderW;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RegResultHeaderWRepo extends JpaRepository<RegResultHeaderW, Long> {
    List<RegResultHeaderW> findAllByStatus(BatchStatusEnum status);
}
