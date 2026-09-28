package com.parksmart.parksmart.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.parksmart.parksmart.dto.CheckOutResponse;
import com.parksmart.parksmart.entity.Booking;
import com.parksmart.parksmart.entity.BookingStatus;
import com.parksmart.parksmart.entity.CheckInOut;
import com.parksmart.parksmart.entity.CheckInOutStatus;
import com.parksmart.parksmart.entity.Slot;
import com.parksmart.parksmart.entity.SlotStatus;
import com.parksmart.parksmart.repository.CheckInOutRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CheckInOutServiceTest {

    @Mock
    private BookingService bookingService;

    @Mock
    private CheckInOutRepository repository;

    private CheckInOutService checkInOutService;

    @BeforeEach
    void setUp() {
        checkInOutService = new CheckInOutService(bookingService, repository, new BigDecimal("5.00"));
    }

    @Test
    void checkoutCalculatesPenaltyAndReleasesSlot() {
        Booking booking = new Booking();
        booking.setStatus(BookingStatus.CHECKED_IN);
        booking.setVehicleNumber("TN01AB1234");
        booking.setEndTime(LocalDateTime.now().minusMinutes(16));
        Slot slot = new Slot();
        slot.setStatus(SlotStatus.OCCUPIED);
        booking.setSlot(slot);

        CheckInOut activity = new CheckInOut();
        activity.setCheckInTime(LocalDateTime.now().minusMinutes(30));
        activity.setStatus(CheckInOutStatus.CHECKED_IN);
        when(bookingService.get(1L)).thenReturn(booking);
        when(repository.findByBookingId(1L)).thenReturn(Optional.of(activity));

        CheckOutResponse response = checkInOutService.checkOut(1L);

        assertEquals(BookingStatus.COMPLETED, response.status());
        assertEquals(SlotStatus.AVAILABLE, slot.getStatus());
        assertEquals(new BigDecimal("5.00"), response.penaltyRatePerMinute());
        assertEquals(response.overstayMinutes() * 5, response.penaltyAmount().longValue());
        verify(repository).save(activity);
    }
}
