package com.parksmart.parksmart.service;

import com.parksmart.parksmart.dto.ParkingLotRequest;
import com.parksmart.parksmart.dto.ParkingLotResponse;
import com.parksmart.parksmart.entity.ParkingLot;
import com.parksmart.parksmart.exception.ResourceNotFoundException;
import com.parksmart.parksmart.repository.ParkingLotRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ParkingLotService {
    private final ParkingLotRepository repository;

    public ParkingLotService(ParkingLotRepository repository) { this.repository = repository; }

    @Transactional
    public ParkingLotResponse create(ParkingLotRequest request) {
        ParkingLot lot = new ParkingLot();
        apply(lot, request);
        return toResponse(repository.save(lot));
    }

    @Transactional(readOnly = true)
    public List<ParkingLotResponse> findAll() { return repository.findAll().stream().map(this::toResponse).toList(); }

    @Transactional(readOnly = true)
    public ParkingLotResponse findById(Long id) { return toResponse(get(id)); }

    @Transactional
    public ParkingLotResponse update(Long id, ParkingLotRequest request) {
        ParkingLot lot = get(id);
        apply(lot, request);
        return toResponse(repository.save(lot));
    }

    @Transactional
    public void delete(Long id) { repository.delete(get(id)); }

    public ParkingLot get(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Parking lot not found: " + id));
    }

    private void apply(ParkingLot lot, ParkingLotRequest request) {
        lot.setName(request.name().trim());
        lot.setLocation(request.location().trim());
        lot.setTotalSlots(request.totalSlots());
    }

    private ParkingLotResponse toResponse(ParkingLot lot) {
        return new ParkingLotResponse(lot.getId(), lot.getName(), lot.getLocation(), lot.getTotalSlots());
    }
}
