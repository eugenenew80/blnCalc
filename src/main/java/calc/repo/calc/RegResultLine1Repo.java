package calc.repo.calc;

import calc.entity.calc.reg.RegResultLine1;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RegResultLine1Repo extends JpaRepository<RegResultLine1, Long> {
    List<RegResultLine1> findAllByHeaderId(Long headerId);

    @Query("select t from RegResultLine1 t where t.header.id = ?1 and t.meteringPoint.code = ?2")
    List<RegResultLine1> findByMeteringPoint(Long headerId, String mpCode);
}
