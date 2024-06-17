package com.example.demo.Repository;
import com.example.demo.model.PressureReading;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PressureReadingRepository extends JpaRepository<PressureReading, Long> {
    List<PressureReading> findAllByTimestampBetween(LocalDateTime start, LocalDateTime end);

}