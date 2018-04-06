package kz.kegoc.bln.repo;

import kz.kegoc.bln.entity.Reactor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReactorRepo extends JpaRepository<Reactor, Long> { }
