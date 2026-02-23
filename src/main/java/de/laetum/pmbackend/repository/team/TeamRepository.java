package de.laetum.pmbackend.repository.team; 

import de.laetum.pmbackend.repository.team.Team; 
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {

  Optional<Team> findByName(String name);

  boolean existsByName(String name);
}
