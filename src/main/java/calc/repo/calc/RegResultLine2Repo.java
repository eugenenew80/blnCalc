package calc.repo.calc;

import calc.entity.calc.reg.RegResultLine2;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RegResultLine2Repo extends JpaRepository<RegResultLine2, Long> {
    List<RegResultLine2> findAllByHeaderId(Long headerId);

    @Query("select t from RegResultLine2 t where t.header.id = ?1 and t.meteringPoint.code = ?2 and t.val is not null")
    List<RegResultLine2> findByMeteringPoint(Long headerId, String mpCode);

    @Query("select t from RegResultLine2 t where t.header.id = ?1 and t.meteringPoint.code = ?2 and t.param = ?3 and t.val is not null")
    List<RegResultLine2> findByParam(Long headerId, String mpCode, String param);
}
