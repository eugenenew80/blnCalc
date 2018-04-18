package calc.repo;

import calc.entity.SourceTypePriority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SourceTypePriorityRepo extends JpaRepository<SourceTypePriority, Long> {
    List<SourceTypePriority> findAllByMeteringPointId(Long meteringPointId);
}
