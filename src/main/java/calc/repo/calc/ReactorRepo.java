package calc.repo.calc;

import calc.entity.calc.Reactor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReactorRepo extends JpaRepository<Reactor, Long> { }
