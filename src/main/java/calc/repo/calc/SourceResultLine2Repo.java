package calc.repo.calc;

import calc.entity.calc.source.SourceResultLine2;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SourceResultLine2Repo extends JpaRepository<SourceResultLine2, Long> {
    List<SourceResultLine2> findAllByHeaderId(Long headerId);

    @Query("select t from SourceResultLine2 t where t.header.id = ?1 and t.meteringPoint.code = ?2 ")
    List<SourceResultLine2> findByMeteringPoint(Long headerId, String mpCode);

    @Query("select t from SourceResultLine2 t where t.header.id = ?1 and t.meteringPoint.code = ?2 and t.param = ?3")
    List<SourceResultLine2> findByParam(Long headerId, String mpCode, String param);
}
