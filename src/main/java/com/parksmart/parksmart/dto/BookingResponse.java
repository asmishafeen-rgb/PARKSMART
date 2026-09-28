package com.parksmart.parksmart.dto;

import com.parksmart.parksmart.entity.BookingStatus;
import java.time.LocalDateTime;

public record BookingResponse(Long id, String userName, String vehicleNumber, Long slotId,
                              String slotNumber, Long parkingLotId, String parkingLotName,
                              LocalDateTime startTime, LocalDateTime endTime,
                              LocalDateTime createdAt, BookingStatus status) {
}
