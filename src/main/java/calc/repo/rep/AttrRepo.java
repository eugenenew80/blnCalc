package calc.repo.rep;

import calc.entity.rep.TableAttr;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AttrRepo extends JpaRepository<TableAttr, Long> { }
