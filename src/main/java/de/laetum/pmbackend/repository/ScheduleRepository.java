package de.laetum.pmbackend.repository;

import de.laetum.pmbackend.entity.Schedule;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

  List<Schedule> findByUserId(Long userId);

  List<Schedule> findByUserIdOrderByDateDesc(Long userId);
}
