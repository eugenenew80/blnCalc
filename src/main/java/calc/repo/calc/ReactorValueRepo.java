package calc.repo.calc;

import calc.entity.calc.bs.pe.ReactorValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReactorValueRepo extends JpaRepository<ReactorValue, Long> {
    List<ReactorValue> findAllByHeaderId(Long headerId);
    List<ReactorValue> findAllByHeaderIdAndReactorId(Long headerId, Long reactorId);
}
