package calc.repo.calc;

import calc.entity.calc.distr.DistrResultHeader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DistrResultHeaderRepo extends JpaRepository<DistrResultHeader, Long> {

    @Query("select t from DistrResultHeader t where t.org.id = ?1 and t.dataType = ?2 and t.startDate = ?3 and t.endDate = ?4 and t.isActive = true ")
    List<DistrResultHeader> findByOrg(Long orgId, String dataType, LocalDate startDate, LocalDate endDate);
}
