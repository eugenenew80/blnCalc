package calc.repo.calc;

import calc.entity.calc.MeteringPoint;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MeteringPointRepo extends JpaRepository<MeteringPoint, Long> {
    MeteringPoint findByCode(String code);
}
