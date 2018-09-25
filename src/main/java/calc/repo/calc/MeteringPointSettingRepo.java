package calc.repo.calc;

import calc.entity.calc.svr.MeteringPointSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface MeteringPointSettingRepo extends JpaRepository<MeteringPointSetting, Long> {
    @Query("select t from MeteringPointSetting t where t.contract.id = ?1 and (((t.startDate is null or t.startDate <= ?2) and (t.endDate is null or t.endDate > ?3)) or t.startDate between ?2 and ?3 or t.endDate between ?2 and ?3) order by t.startDate, t.endDate" )
    List<MeteringPointSetting> findAllByContractIdAndDate(Long contractId, LocalDate startDate, LocalDate endDateTime);

}
