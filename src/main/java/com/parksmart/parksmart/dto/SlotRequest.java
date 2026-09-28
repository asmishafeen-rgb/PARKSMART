package com.parksmart.parksmart.dto;

import com.parksmart.parksmart.entity.SlotType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record SlotRequest(@NotBlank(message = "Slot number is required") String slotNumber,
                          @NotNull(message = "Slot type is required") SlotType slotType,
                          @NotNull(message = "Parking lot ID is required") @Positive(message = "Parking lot ID must be positive") Long parkingLotId) {
}
