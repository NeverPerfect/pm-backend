package de.laetum.pmbackend.repository.team;

import de.laetum.pmbackend.repository.team.Team;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {

  Optional<Team> findByName(String name);

  boolean existsByUsersId(Long userId);

  List<Team> findAllByUsersId(Long userId);
}
