package calc.repo.calc;

import calc.entity.calc.inter.InterResultLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface InterResultLineRepo extends JpaRepository<InterResultLine, Long> {
    List<InterResultLine> findAllByHeaderId(Long headerId);

    @Query("select t from InterResultLine t where t.header.id = ?1 and t.powerLine.id = ?2")
    List<InterResultLine> findByPowerLine(Long headerId, Long powerLineId);
}
