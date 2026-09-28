package com.parksmart.parksmart.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.parksmart.parksmart.dto.BookingRequest;
import com.parksmart.parksmart.entity.BookingStatus;
import com.parksmart.parksmart.entity.Slot;
import com.parksmart.parksmart.exception.SlotAlreadyBookedException;
import com.parksmart.parksmart.repository.BookingRepository;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository repository;

    @Mock
    private SlotService slotService;

    @InjectMocks
    private BookingService bookingService;

    @Test
    void rejectsOverlappingBookingBeforeSaving() {
        Slot slot = new Slot();
        BookingRequest request = new BookingRequest("John", "TN01AB1234", 1L,
                LocalDateTime.of(2026, 9, 28, 10, 0),
                LocalDateTime.of(2026, 9, 28, 12, 0));
        when(slotService.get(1L)).thenReturn(slot);
        when(repository.existsOverlappingBooking(any(), eq(request.startTime()), eq(request.endTime()), eq(BookingStatus.CANCELLED)))
                .thenReturn(true);

        assertThrows(SlotAlreadyBookedException.class, () -> bookingService.create(request));
        verify(repository, never()).save(any());
    }
}
