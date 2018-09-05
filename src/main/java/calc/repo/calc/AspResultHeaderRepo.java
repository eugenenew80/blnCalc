package calc.repo.calc;

import calc.entity.calc.AspResultHeader;
import calc.entity.calc.enums.BatchStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AspResultHeaderRepo extends JpaRepository<AspResultHeader, Long> {
    List<AspResultHeader> findAllByStatus(BatchStatusEnum status);
}
