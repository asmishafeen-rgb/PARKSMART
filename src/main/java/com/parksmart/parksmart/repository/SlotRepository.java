package com.parksmart.parksmart.repository;

import com.parksmart.parksmart.entity.Slot;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SlotRepository extends JpaRepository<Slot, Long> {

    @Query("""
        select s from Slot s
        where s.status <> com.parksmart.parksmart.entity.SlotStatus.MAINTENANCE
          and not exists (
            select b.id from Booking b
            where b.slot = s
              and b.status <> com.parksmart.parksmart.entity.BookingStatus.CANCELLED
              and b.startTime < :endTime
              and b.endTime > :startTime
          )
        order by s.parkingLot.id, s.slotNumber
        """)
    List<Slot> findAvailable(@Param("startTime") LocalDateTime startTime,
                             @Param("endTime") LocalDateTime endTime);
}
