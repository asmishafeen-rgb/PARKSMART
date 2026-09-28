package com.parksmart.parksmart.dto;

import com.parksmart.parksmart.entity.BookingStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CheckOutResponse(Long bookingId, String vehicleNumber, LocalDateTime checkInTime,
                               LocalDateTime bookedEndTime, LocalDateTime checkOutTime,
                               Long overstayMinutes, BigDecimal penaltyRatePerMinute,
                               BigDecimal penaltyAmount, BookingStatus status) {
}
