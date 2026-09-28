package com.parksmart.parksmart.service;

import com.parksmart.parksmart.dto.SlotRequest;
import com.parksmart.parksmart.dto.SlotResponse;
import com.parksmart.parksmart.entity.Slot;
import com.parksmart.parksmart.entity.SlotStatus;
import com.parksmart.parksmart.exception.ResourceNotFoundException;
import com.parksmart.parksmart.repository.SlotRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SlotService {
    private final SlotRepository repository;
    private final ParkingLotService parkingLotService;

    public SlotService(SlotRepository repository, ParkingLotService parkingLotService) {
        this.repository = repository;
        this.parkingLotService = parkingLotService;
    }

    @Transactional
    public SlotResponse create(SlotRequest request) {
        Slot slot = new Slot();
        slot.setParkingLot(parkingLotService.get(request.parkingLotId()));
        apply(slot, request);
        return toResponse(repository.save(slot));
    }

    @Transactional(readOnly = true)
    public List<SlotResponse> findAll() { return repository.findAll().stream().map(this::toResponse).toList(); }

    @Transactional(readOnly = true)
    public SlotResponse findById(Long id) { return toResponse(get(id)); }

    @Transactional(readOnly = true)
    public List<SlotResponse> findAvailable(LocalDateTime startTime, LocalDateTime endTime) {
        validateWindow(startTime, endTime);
        return repository.findAvailable(startTime, endTime).stream().map(this::toResponse).toList();
    }

    @Transactional
    public SlotResponse update(Long id, SlotRequest request) {
        Slot slot = get(id);
        slot.setParkingLot(parkingLotService.get(request.parkingLotId()));
        apply(slot, request);
        return toResponse(repository.save(slot));
    }

    @Transactional
    public void delete(Long id) { repository.delete(get(id)); }

    public Slot get(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Slot not found: " + id));
    }

    private void apply(Slot slot, SlotRequest request) {
        slot.setSlotNumber(request.slotNumber().trim());
        slot.setSlotType(request.slotType());
        if (slot.getStatus() == null) slot.setStatus(SlotStatus.AVAILABLE);
    }

    private void validateWindow(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null || !startTime.isBefore(endTime)) {
            throw new IllegalArgumentException("Start time must be before end time.");
        }
    }

    private SlotResponse toResponse(Slot slot) {
        return new SlotResponse(slot.getId(), slot.getSlotNumber(), slot.getSlotType(), slot.getStatus(),
                slot.getParkingLot().getId(), slot.getParkingLot().getName(), slot.getParkingLot().getLocation());
    }
}
