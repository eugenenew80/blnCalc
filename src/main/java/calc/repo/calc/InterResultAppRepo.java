package calc.repo.calc;

import calc.entity.calc.inter.InterResultApp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface InterResultAppRepo extends JpaRepository<InterResultApp, Long> {
    List<InterResultApp> findAllByHeaderId(Long headerId);
}
