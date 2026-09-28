package com.parksmart.parksmart.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record ParkingLotRequest(@NotBlank(message = "Name is required") String name,
                                @NotBlank(message = "Location is required") String location,
                                @NotNull(message = "Total slots is required") @PositiveOrZero(message = "Total slots cannot be negative") Integer totalSlots) {
}
