package kz.kegoc.bln.repo;

import kz.kegoc.bln.entity.MeteringPoint;
import kz.kegoc.bln.entity.Parameter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MeteringPointRepo extends JpaRepository<MeteringPoint, Long> {
    MeteringPoint findByCode(String code);
}
