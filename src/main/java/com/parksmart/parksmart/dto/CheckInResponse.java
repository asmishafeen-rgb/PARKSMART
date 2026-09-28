package com.parksmart.parksmart.dto;

import com.parksmart.parksmart.entity.BookingStatus;
import java.time.LocalDateTime;

public record CheckInResponse(Long bookingId, String vehicleNumber, LocalDateTime checkInTime,
                              BookingStatus status) {
}
