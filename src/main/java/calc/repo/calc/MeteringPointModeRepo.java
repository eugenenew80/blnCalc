package calc.repo.calc;

import calc.entity.calc.MeteringPointMode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MeteringPointModeRepo extends JpaRepository<MeteringPointMode, Long> { }
