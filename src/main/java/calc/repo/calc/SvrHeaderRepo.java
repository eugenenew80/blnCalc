package calc.repo.calc;

import calc.entity.calc.svr.SvrResultHeader;
import calc.entity.calc.enums.BatchStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SvrHeaderRepo extends JpaRepository<SvrResultHeader, Long> {
    List<SvrResultHeader> findAllByStatus(BatchStatusEnum status);
}
