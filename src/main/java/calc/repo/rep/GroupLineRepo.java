package calc.repo.rep;

import calc.entity.rep.GroupLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupLineRepo extends JpaRepository<GroupLine, Long> { }
