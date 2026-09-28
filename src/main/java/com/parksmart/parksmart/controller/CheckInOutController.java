package com.parksmart.parksmart.controller;

import com.parksmart.parksmart.dto.CheckInResponse;
import com.parksmart.parksmart.dto.CheckOutResponse;
import com.parksmart.parksmart.service.CheckInOutService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class CheckInOutController {
    private final CheckInOutService service;
    public CheckInOutController(CheckInOutService service) { this.service = service; }

    @PostMapping("/check-in/{bookingId}")
    public CheckInResponse checkIn(@PathVariable Long bookingId) { return service.checkIn(bookingId); }

    @PostMapping("/check-out/{bookingId}")
    public CheckOutResponse checkOut(@PathVariable Long bookingId) { return service.checkOut(bookingId); }
}
