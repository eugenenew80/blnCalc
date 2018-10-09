package calc.repo.calc;

import calc.entity.calc.asp.AspResultApp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AspResultAppRepo extends JpaRepository<AspResultApp, Long> {
    List<AspResultApp> findAllByHeaderId(Long headerId);
}
