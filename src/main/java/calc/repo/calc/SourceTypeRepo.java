package calc.repo.calc;

import calc.entity.calc.SourceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SourceTypeRepo extends JpaRepository<SourceType, Long> { }
