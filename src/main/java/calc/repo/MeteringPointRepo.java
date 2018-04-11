package calc.repo;

import calc.entity.MeteringPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MeteringPointRepo extends JpaRepository<MeteringPoint, Long> {
    MeteringPoint findByCode(String code);
}
