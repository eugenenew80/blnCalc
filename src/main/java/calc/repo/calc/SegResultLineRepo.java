package calc.repo.calc;

import calc.entity.calc.seg.SegResultLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SegResultLineRepo extends JpaRepository<SegResultLine, Long> {
    List<SegResultLine> findAllByHeaderId(Long headerId);

    @Query("select t from SegResultLine t where t.header.id = ?1 and t.meteringPoint.code = ?2 and t.val is not null")
    List<SegResultLine> findByMeteringPoint(Long headerId, String mpCode);

    @Query("select t from SegResultLine t where t.header.id = ?1 and t.meteringPoint.code = ?2 and t.param = ?3 and t.val is not null")
    List<SegResultLine> findByParam(Long headerId, String mpCode, String param);
}
