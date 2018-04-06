package kz.kegoc.bln.repo;

import kz.kegoc.bln.entity.PowerLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PowerLineRepo extends JpaRepository<PowerLine, Long> {
    PowerLine findByCode(String code);
}
