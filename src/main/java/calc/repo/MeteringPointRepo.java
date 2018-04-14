package calc.repo;

import calc.entity.MeteringPoint;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MeteringPointRepo extends JpaRepository<MeteringPoint, Long> {
    @Cacheable(value="meteringPointsCache", key="{#p0}" )
    MeteringPoint findByCode(String code);
}
