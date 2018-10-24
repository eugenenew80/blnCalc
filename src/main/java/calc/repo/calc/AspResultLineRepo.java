package calc.repo.calc;

import calc.entity.calc.asp.AspResultLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AspResultLineRepo extends JpaRepository<AspResultLine, Long> {
    List<AspResultLine> findAllByHeaderId(Long headerId);

    @Query("select t from AspResultLine t where t.header.id = ?1 and t.meteringPoint.code = ?2")
    List<AspResultLine> findByMeteringPoint(Long headerId, String mpCode);

    @Query("select t from AspResultLine t where t.header.id = ?1 and t.meteringPoint.code = ?2 and t.param = ?3")
    List<AspResultLine> findByParam(Long headerId, String mpCode, String param);
}
