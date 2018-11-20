package calc.repo.calc;

import calc.entity.calc.source.SourceResultLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SourceResultLineRepo extends JpaRepository<SourceResultLine, Long> {
    List<SourceResultLine> findAllByHeaderId(Long headerId);

    @Query("select t from SourceResultLine t where t.header.id = ?1 and t.meteringPoint.code = ?2 and t.val is not null")
    List<SourceResultLine> findByMeteringPoint(Long headerId, String mpCode);

    @Query("select t from SourceResultLine t where t.header.id = ?1 and t.meteringPoint.code = ?2 and t.param = ?3 and t.val is not null")
    List<SourceResultLine> findByParam(Long headerId, String mpCode, String param);
}
