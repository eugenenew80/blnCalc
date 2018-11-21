package calc.repo.calc;

import calc.entity.calc.reg.RegResultLine3;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RegResultLine3Repo extends JpaRepository<RegResultLine3, Long> {
    List<RegResultLine3> findAllByHeaderId(Long headerId);

    @Query("select t from RegResultLine3 t where t.header.id = ?1 and t.meteringPoint.code = ?2")
    List<RegResultLine3> findByMeteringPoint(Long headerId, String mpCode);

    @Query("select t from RegResultLine3 t where t.header.id = ?1 and t.meteringPoint.code = ?2 and t.param = ?3")
    List<RegResultLine3> findByParam(Long headerId, String mpCode, String param);
}
