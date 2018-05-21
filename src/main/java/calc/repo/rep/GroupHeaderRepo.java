package calc.repo.rep;

import calc.entity.rep.GroupHeader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupHeaderRepo extends JpaRepository<GroupHeader, Long> { }
