package calc.repo.calc;

import calc.entity.calc.svr.MeteringPointSettingNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MeteringPointSettingNoteRepo extends JpaRepository<MeteringPointSettingNote, Long> {
    List<MeteringPointSettingNote> findAllByContractId(Long contractId);
}
