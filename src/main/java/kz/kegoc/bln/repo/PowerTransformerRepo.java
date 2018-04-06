package kz.kegoc.bln.repo;

import kz.kegoc.bln.entity.PowerTransformer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PowerTransformerRepo extends JpaRepository<PowerTransformer, Long> { }
