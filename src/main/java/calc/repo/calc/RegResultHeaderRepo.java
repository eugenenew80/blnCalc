package calc.repo.calc;

import calc.entity.calc.reg.RegResultHeader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RegResultHeaderRepo extends JpaRepository<RegResultHeader, Long> { }
