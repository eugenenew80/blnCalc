package calc.repo.calc;

import calc.entity.calc.asp.AspResultHeaderW;
import calc.entity.calc.enums.BatchStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AspResultHeaderWRepo extends JpaRepository<AspResultHeaderW, Long> {
    List<AspResultHeaderW> findAllByStatus(BatchStatusEnum status);
}
