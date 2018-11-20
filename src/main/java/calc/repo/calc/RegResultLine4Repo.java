package calc.repo.calc;

import calc.entity.calc.reg.RegResultLine4;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RegResultLine4Repo extends JpaRepository<RegResultLine4, Long> {
    List<RegResultLine4> findAllByHeaderId(Long headerId);

    @Query("select t from RegResultLine4 t where t.header.id = ?1 and t.meteringPoint.code = ?2 and t.val is not null")
    List<RegResultLine4> findByMeteringPoint(Long headerId, String mpCode);
}
