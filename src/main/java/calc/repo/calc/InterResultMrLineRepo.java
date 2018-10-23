package calc.repo.calc;

import calc.entity.calc.inter.InterResultMrLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface InterResultMrLineRepo extends JpaRepository<InterResultMrLine, Long> {
    List<InterResultMrLine> findAllByHeaderId(Long headerId);

    @Query("select t from InterResultMrLine t where t.header.id = ?1 and t.meteringPoint.code = ?2")
    List<InterResultMrLine> findByMeteringPoint(Long headerId, String mpCode);

    @Query("select t from InterResultMrLine t where t.header.id = ?1 and t.meteringPoint.code = ?2 and t.param = ?3")
    List<InterResultMrLine> findByParam(Long headerId, String mpCode, String param);
}
