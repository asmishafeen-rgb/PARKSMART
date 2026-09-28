package com.parksmart.parksmart.repository;

import com.parksmart.parksmart.entity.CheckInOut;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CheckInOutRepository extends JpaRepository<CheckInOut, Long> {
    Optional<CheckInOut> findByBookingId(Long bookingId);
}
