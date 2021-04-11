package net.gogroups.gowaka.domain.repository;

import net.gogroups.gowaka.domain.model.SeatStructure;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeatStructureRepository extends JpaRepository<SeatStructure, Long> {
    List<SeatStructure> findAllByNumberOfSeats(Integer numberOfSeats);
    List<SeatStructure> findBySeatStructureCode(String seatStructureCode); // typically a single item in a list
}
