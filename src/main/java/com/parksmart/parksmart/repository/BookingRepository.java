package com.parksmart.parksmart.repository;

import com.parksmart.parksmart.entity.Booking;
import com.parksmart.parksmart.entity.BookingStatus;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("""
        select case when count(b) > 0 then true else false end
        from Booking b
        where b.slot.id = :slotId
          and b.status <> :cancelledStatus
          and b.startTime < :endTime
          and b.endTime > :startTime
        """)
    boolean existsOverlappingBooking(@Param("slotId") Long slotId,
                                     @Param("startTime") LocalDateTime startTime,
                                     @Param("endTime") LocalDateTime endTime,
                                     @Param("cancelledStatus") BookingStatus cancelledStatus);

    @Query("""
        select case when count(b) > 0 then true else false end
        from Booking b
        where b.id <> :bookingId
          and b.slot.id = :slotId
          and b.status <> :cancelledStatus
          and b.startTime < :endTime
          and b.endTime > :startTime
        """)
    boolean existsOverlappingBookingForUpdate(@Param("bookingId") Long bookingId,
                                              @Param("slotId") Long slotId,
                                              @Param("startTime") LocalDateTime startTime,
                                              @Param("endTime") LocalDateTime endTime,
                                              @Param("cancelledStatus") BookingStatus cancelledStatus);

    @Query("""
      select b from Booking b
      where b.slot.id = :slotId
      order by b.startTime asc
      """)
    List<Booking> findBookingsForSlot(@Param("slotId") Long slotId);
}
