package de.laetum.pmbackend.repository;

import de.laetum.pmbackend.entity.Project;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

  Optional<Project> findByName(String name);

  boolean existsByName(String name);

  List<Project> findByActiveTrue();
}
