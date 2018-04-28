package calc.repo.calc;

import calc.entity.calc.PowerLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PowerLineRepo extends JpaRepository<PowerLine, Long> {
    PowerLine findByCode(String code);
}
