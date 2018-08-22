package calc.repo.calc;

import calc.entity.calc.Undercount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UndercountRepo extends JpaRepository<Undercount, Long> {

    @Query("select u from Undercount u where u.meteringPoint.id = ?1 and (u.startDate is null or u.startDate <= ?2) and (u.endDate is null or u.endDate > ?2) order by u.startDate, u.endDate" )
    List<Undercount> findAllByMeteringPointIdAndDate(Long meteringPointId, LocalDateTime dateTime);
}
