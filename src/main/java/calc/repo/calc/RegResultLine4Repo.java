package calc.repo.calc;

import calc.entity.calc.reg.RegResultLine4;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RegResultLine4Repo extends JpaRepository<RegResultLine4, Long> {
    List<RegResultLine4> findAllByHeaderId(Long headerId);

    @Query("select t from RegResultLine4 t where t.header.id = ?1 and t.dealer.id = ?2")
    List<RegResultLine4> findByDealer(Long headerId, Long dealerId);
}
