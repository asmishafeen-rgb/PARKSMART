package com.parksmart.parksmart.controller;

import com.parksmart.parksmart.dto.SlotRequest;
import com.parksmart.parksmart.dto.SlotResponse;
import com.parksmart.parksmart.service.SlotService;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/slots")
public class SlotController {
    private final SlotService service;
    public SlotController(SlotService service) { this.service = service; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SlotResponse create(@Valid @RequestBody SlotRequest request) { return service.create(request); }
    @GetMapping
    public List<SlotResponse> findAll() { return service.findAll(); }
    @GetMapping("/available")
    public List<SlotResponse> findAvailable(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        return service.findAvailable(startTime, endTime);
    }
    @GetMapping("/{id}")
    public SlotResponse findById(@PathVariable Long id) { return service.findById(id); }
    @PutMapping("/{id}")
    public SlotResponse update(@PathVariable Long id, @Valid @RequestBody SlotRequest request) { return service.update(id, request); }
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) { service.delete(id); }
}
