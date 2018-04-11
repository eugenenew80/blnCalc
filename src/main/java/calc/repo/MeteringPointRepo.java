package calc.repo;

import calc.entity.MeteringPoint;
import calc.entity.Parameter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MeteringPointRepo extends JpaRepository<MeteringPoint, Long> {
    MeteringPoint findByCode(String code);
    List<MeteringPoint> findAllByOrgId(Long orgId);
}
