package calc.repo.calc;

import calc.entity.calc.PowerTransformerValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PowerTransformerValueRepo extends JpaRepository<PowerTransformerValue, Long> {
    List<PowerTransformerValue> findAllByHeaderId(Long headerId);
}
