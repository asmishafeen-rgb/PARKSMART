package com.parksmart.parksmart.service;

import com.parksmart.parksmart.dto.CheckInResponse;
import com.parksmart.parksmart.dto.CheckOutResponse;
import com.parksmart.parksmart.entity.Booking;
import com.parksmart.parksmart.entity.BookingStatus;
import com.parksmart.parksmart.entity.CheckInOut;
import com.parksmart.parksmart.entity.CheckInOutStatus;
import com.parksmart.parksmart.entity.SlotStatus;
import com.parksmart.parksmart.exception.InvalidCheckInException;
import com.parksmart.parksmart.exception.InvalidCheckOutException;
import com.parksmart.parksmart.repository.CheckInOutRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CheckInOutService {
    private final BookingService bookingService;
    private final CheckInOutRepository repository;
    private final BigDecimal penaltyRatePerMinute;

    public CheckInOutService(BookingService bookingService, CheckInOutRepository repository,
                             @Value("${parksmart.penalty-rate-per-minute:5.00}") BigDecimal penaltyRatePerMinute) {
        this.bookingService = bookingService;
        this.repository = repository;
        this.penaltyRatePerMinute = penaltyRatePerMinute;
    }

    @Transactional
    public CheckInResponse checkIn(Long bookingId) {
        Booking booking = bookingService.get(bookingId);
        if (booking.getStatus() != BookingStatus.BOOKED) throw new InvalidCheckInException("Only BOOKED reservations can be checked in.");
        if (repository.findByBookingId(bookingId).isPresent()) throw new InvalidCheckInException("This booking already has check-in activity.");
        LocalDateTime checkInTime = LocalDateTime.now();
        CheckInOut activity = new CheckInOut();
        activity.setBooking(booking);
        activity.setCheckInTime(checkInTime);
        activity.setStatus(CheckInOutStatus.CHECKED_IN);
        repository.save(activity);
        booking.setStatus(BookingStatus.CHECKED_IN);
        booking.getSlot().setStatus(SlotStatus.OCCUPIED);
        return new CheckInResponse(booking.getId(), booking.getVehicleNumber(), checkInTime, booking.getStatus());
    }

    @Transactional
    public CheckOutResponse checkOut(Long bookingId) {
        Booking booking = bookingService.get(bookingId);
        if (booking.getStatus() != BookingStatus.CHECKED_IN) throw new InvalidCheckOutException("Vehicle must be checked in before checkout.");
        CheckInOut activity = repository.findByBookingId(bookingId)
                .orElseThrow(() -> new InvalidCheckOutException("Check-in record not found for this booking."));
        LocalDateTime checkout = LocalDateTime.now();
        long overstayMinutes = checkout.isAfter(booking.getEndTime()) ? ChronoUnit.MINUTES.between(booking.getEndTime(), checkout) : 0;
        BigDecimal penalty = penaltyRatePerMinute.multiply(BigDecimal.valueOf(overstayMinutes)).setScale(2, RoundingMode.HALF_UP);
        activity.setCheckOutTime(checkout);
        activity.setOverstayMinutes(overstayMinutes);
        activity.setPenaltyAmount(penalty);
        activity.setStatus(CheckInOutStatus.CHECKED_OUT);
        repository.save(activity);
        booking.setStatus(BookingStatus.COMPLETED);
        booking.getSlot().setStatus(SlotStatus.AVAILABLE);
        return new CheckOutResponse(booking.getId(), booking.getVehicleNumber(), activity.getCheckInTime(), booking.getEndTime(),
                checkout, overstayMinutes, penaltyRatePerMinute, penalty, booking.getStatus());
    }
}
