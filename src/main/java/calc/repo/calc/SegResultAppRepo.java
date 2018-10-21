package calc.repo.calc;

import calc.entity.calc.seg.SegResultApp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SegResultAppRepo extends JpaRepository<SegResultApp, Long> {
    List<SegResultApp> findAllByHeaderId(Long headerId);
}
