package calc.repo.calc;

import calc.entity.calc.asp.AspResultHeader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AspResultHeaderRepo extends JpaRepository<AspResultHeader, Long> { }
