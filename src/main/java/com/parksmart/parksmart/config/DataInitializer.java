package com.parksmart.parksmart.config;

import com.parksmart.parksmart.entity.ParkingLot;
import com.parksmart.parksmart.entity.Slot;
import com.parksmart.parksmart.entity.SlotType;
import com.parksmart.parksmart.repository.ParkingLotRepository;
import com.parksmart.parksmart.repository.SlotRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {
    @Bean
    CommandLineRunner seedSampleData(ParkingLotRepository lotRepository, SlotRepository slotRepository) {
        return args -> {
            if (lotRepository.count() > 0) return;
            ParkingLot lot = new ParkingLot();
            lot.setName("Main Parking Area");
            lot.setLocation("College Campus");
            lot.setTotalSlots(5);
            lot = lotRepository.save(lot);
            for (int i = 1; i <= 5; i++) {
                Slot slot = new Slot();
                slot.setSlotNumber("A0" + i);
                slot.setSlotType(SlotType.CAR);
                slot.setParkingLot(lot);
                slotRepository.save(slot);
            }
        };
    }
}
