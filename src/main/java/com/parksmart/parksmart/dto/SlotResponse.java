package com.parksmart.parksmart.dto;

import com.parksmart.parksmart.entity.SlotStatus;
import com.parksmart.parksmart.entity.SlotType;

public record SlotResponse(Long id, String slotNumber, SlotType slotType, SlotStatus status,
                           Long parkingLotId, String parkingLotName, String location) {
}
