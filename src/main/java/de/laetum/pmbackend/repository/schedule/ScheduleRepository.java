package de.laetum.pmbackend.repository.schedule;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

  List<Schedule> findByUserId(Long userId);

  List<Schedule> findByUserIdOrderByDateDesc(Long userId);

  boolean existsByUserId(Long userId);

  boolean existsByTeamId(Long teamId);

  boolean existsByProjectId(Long projectId);

  boolean existsByCategoryId(Long categoryId);
}