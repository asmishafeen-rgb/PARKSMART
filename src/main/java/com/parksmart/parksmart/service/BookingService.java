package com.parksmart.parksmart.service;

import com.parksmart.parksmart.dto.BookingRequest;
import com.parksmart.parksmart.dto.BookingResponse;
import com.parksmart.parksmart.entity.Booking;
import com.parksmart.parksmart.entity.BookingStatus;
import com.parksmart.parksmart.entity.Slot;
import com.parksmart.parksmart.exception.InvalidBookingException;
import com.parksmart.parksmart.exception.ResourceNotFoundException;
import com.parksmart.parksmart.exception.SlotAlreadyBookedException;
import com.parksmart.parksmart.repository.BookingRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookingService {
    private final BookingRepository repository;
    private final SlotService slotService;

    public BookingService(BookingRepository repository, SlotService slotService) {
        this.repository = repository;
        this.slotService = slotService;
    }

    @Transactional
    public BookingResponse create(BookingRequest request) {
        validateWindow(request);
        Slot slot = slotService.get(request.slotId());
        ensureAvailable(slot, request);
        Booking booking = new Booking();
        booking.setSlot(slot);
        apply(booking, request);
        return toResponse(repository.save(booking));
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> findAll() { return repository.findAll().stream().map(this::toResponse).toList(); }

    @Transactional(readOnly = true)
    public BookingResponse findById(Long id) { return toResponse(get(id)); }

    @Transactional
    public BookingResponse update(Long id, BookingRequest request) {
        validateWindow(request);
        Booking booking = get(id);
        if (booking.getStatus() != BookingStatus.BOOKED) throw new InvalidBookingException("Only BOOKED reservations can be edited.");
        Slot slot = slotService.get(request.slotId());
        if (repository.existsOverlappingBookingForUpdate(id, slot.getId(), request.startTime(), request.endTime(), BookingStatus.CANCELLED)) {
            throw new SlotAlreadyBookedException("Parking slot is already booked for the selected time window.");
        }
        booking.setSlot(slot);
        apply(booking, request);
        return toResponse(repository.save(booking));
    }

    @Transactional
    public void cancel(Long id) {
        Booking booking = get(id);
        if (booking.getStatus() == BookingStatus.CHECKED_IN) throw new InvalidBookingException("A checked-in booking cannot be cancelled.");
        booking.setStatus(BookingStatus.CANCELLED);
        repository.save(booking);
    }

    public Booking get(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + id));
    }

    private void ensureAvailable(Slot slot, BookingRequest request) {
        if (repository.existsOverlappingBooking(slot.getId(), request.startTime(), request.endTime(), BookingStatus.CANCELLED)) {
            throw new SlotAlreadyBookedException("Parking slot is already booked for the selected time window.");
        }
    }

    private void validateWindow(BookingRequest request) {
        if (request.startTime() == null || request.endTime() == null || !request.startTime().isBefore(request.endTime())) {
            throw new InvalidBookingException("Start time must be before end time.");
        }
    }

    private void apply(Booking booking, BookingRequest request) {
        booking.setUserName(request.userName().trim());
        booking.setVehicleNumber(request.vehicleNumber().trim().toUpperCase());
        booking.setStartTime(request.startTime());
        booking.setEndTime(request.endTime());
    }

    private BookingResponse toResponse(Booking booking) {
        Slot slot = booking.getSlot();
        return new BookingResponse(booking.getId(), booking.getUserName(), booking.getVehicleNumber(), slot.getId(),
                slot.getSlotNumber(), slot.getParkingLot().getId(), slot.getParkingLot().getName(), booking.getStartTime(),
                booking.getEndTime(), booking.getCreatedAt(), booking.getStatus());
    }
}
