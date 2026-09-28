package com.parksmart.parksmart.controller;

import com.parksmart.parksmart.dto.ParkingLotRequest;
import com.parksmart.parksmart.dto.ParkingLotResponse;
import com.parksmart.parksmart.service.ParkingLotService;
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
@RequestMapping("/api/parking-lots")
public class ParkingLotController {
    private final ParkingLotService service;
    public ParkingLotController(ParkingLotService service) { this.service = service; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ParkingLotResponse create(@Valid @RequestBody ParkingLotRequest request) { return service.create(request); }
    @GetMapping
    public List<ParkingLotResponse> findAll() { return service.findAll(); }
    @GetMapping("/{id}")
    public ParkingLotResponse findById(@PathVariable Long id) { return service.findById(id); }
    @PutMapping("/{id}")
    public ParkingLotResponse update(@PathVariable Long id, @Valid @RequestBody ParkingLotRequest request) { return service.update(id, request); }
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) { service.delete(id); }
}
