package calc.repo.calc;

import calc.entity.calc.PowerTransformer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PowerTransformerRepo extends JpaRepository<PowerTransformer, Long> { }
