package com.smartpark.config;

import com.smartpark.entity.*;
import com.smartpark.repository.ParkingSlotRepository;
import com.smartpark.repository.ParkingZoneRepository;
import com.smartpark.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final ParkingZoneRepository zoneRepository;
    private final ParkingSlotRepository slotRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${smartpark.admin.default-email:admin@smartpark.edu}")
    private String adminEmail;

    @Value("${smartpark.admin.default-password:Admin@123456}")
    private String adminPassword;

    @Value("${smartpark.admin.default-name:System Administrator}")
    private String adminName;

    @Value("${smartpark.admin.default-id:ADM-001}")
    private String adminId;

    public DataInitializer(UserRepository userRepository,
                           ParkingZoneRepository zoneRepository,
                           ParkingSlotRepository slotRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.zoneRepository = zoneRepository;
        this.slotRepository = slotRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        initAdminUser();
        initParkingZonesAndSlots();
    }

    private void initAdminUser() {
        if (!userRepository.existsByEmail(adminEmail)) {
            User admin = new User(
                    adminEmail,
                    adminId,
                    adminName,
                    "+1-555-0100",
                    passwordEncoder.encode(adminPassword),
                    Role.ADMIN
            );
            userRepository.save(admin);
            log.info("Initialized default administrator account: {}", adminEmail);
        }
    }

    private void initParkingZonesAndSlots() {
        if (zoneRepository.count() == 0) {
            log.info("Seeding initial campus parking zones and slots...");

            // Zone A - Main Ground Lot
            ParkingZone zoneA = new ParkingZone("ZONE-A", "Main Campus Lot", "Ground Floor", "Standard vehicle parking near Academic Block");
            zoneA = zoneRepository.save(zoneA);

            for (int i = 1; i <= 8; i++) {
                String slotNum = String.format("A-%03d", i);
                ParkingSlot slot = new ParkingSlot(slotNum, zoneA, SlotStatus.AVAILABLE, VehicleType.CAR);
                slotRepository.save(slot);
            }

            // Zone B - Multi-Level Deck
            ParkingZone zoneB = new ParkingZone("ZONE-B", "North Tower Deck", "Level 1", "Covered parking with standard bays");
            zoneB = zoneRepository.save(zoneB);

            for (int i = 1; i <= 8; i++) {
                String slotNum = String.format("B-%03d", i);
                ParkingSlot slot = new ParkingSlot(slotNum, zoneB, SlotStatus.AVAILABLE, VehicleType.CAR);
                slotRepository.save(slot);
            }

            // Zone C - Eco & Two-Wheeler Bay
            ParkingZone zoneC = new ParkingZone("ZONE-C", "Eco & Two-Wheeler Bay", "Ground Floor East", "Dedicated for EV charging and motorcycles");
            zoneC = zoneRepository.save(zoneC);

            for (int i = 1; i <= 4; i++) {
                String slotNum = String.format("C-EV-%02d", i);
                ParkingSlot slot = new ParkingSlot(slotNum, zoneC, SlotStatus.AVAILABLE, VehicleType.EV);
                slotRepository.save(slot);
            }

            for (int i = 5; i <= 8; i++) {
                String slotNum = String.format("C-BK-%02d", i);
                ParkingSlot slot = new ParkingSlot(slotNum, zoneC, SlotStatus.AVAILABLE, VehicleType.BIKE);
                slotRepository.save(slot);
            }

            log.info("Finished seeding initial parking zones and slots.");
        }
    }
}
