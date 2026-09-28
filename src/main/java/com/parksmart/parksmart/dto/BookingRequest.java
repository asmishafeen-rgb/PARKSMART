package com.parksmart.parksmart.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;

public record BookingRequest(@NotBlank(message = "User name is required") String userName,
                             @NotBlank(message = "Vehicle number is required") String vehicleNumber,
                             @NotNull(message = "Slot ID is required") @Positive(message = "Slot ID must be positive") Long slotId,
                             @NotNull(message = "Start time is required") LocalDateTime startTime,
                             @NotNull(message = "End time is required") LocalDateTime endTime) {
}
