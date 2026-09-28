package com.parksmart.parksmart.controller;

import com.parksmart.parksmart.dto.BookingRequest;
import com.parksmart.parksmart.dto.BookingResponse;
import com.parksmart.parksmart.service.BookingService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {
    private final BookingService service;
    public BookingController(BookingService service) { this.service = service; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse create(@Valid @RequestBody BookingRequest request) { return service.create(request); }
    @GetMapping
    public List<BookingResponse> findAll() { return service.findAll(); }
    @GetMapping("/{id}")
    public BookingResponse findById(@PathVariable Long id) { return service.findById(id); }
    @PutMapping("/{id}")
    public BookingResponse update(@PathVariable Long id, @Valid @RequestBody BookingRequest request) { return service.update(id, request); }
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(@PathVariable Long id) { service.cancel(id); }
}
