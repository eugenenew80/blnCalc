package calc.repo.calc;

import calc.entity.calc.inter.InterResultHeader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InterResultHeaderRepo extends JpaRepository<InterResultHeader, Long> { }
