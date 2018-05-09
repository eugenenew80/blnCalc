package calc.repo.rep;

import calc.entity.rep.TableSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SectionRepo extends JpaRepository<TableSection, Long> { }
