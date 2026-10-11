package com.smartpark.service;

import com.opencsv.CSVWriter;
import com.smartpark.entity.ParkingSession;
import com.smartpark.repository.ParkingSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ReportService {

    private final ParkingSessionRepository sessionRepository;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ReportService(ParkingSessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    @Transactional(readOnly = true)
    public byte[] exportParkingSessionsToCsv(LocalDate fromDate, LocalDate toDate) {
        LocalDateTime start = fromDate != null ? fromDate.atStartOfDay() : null;
        LocalDateTime end = toDate != null ? toDate.atTime(LocalTime.MAX) : null;

        List<ParkingSession> sessions = sessionRepository.findAllForReport(start, end);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             OutputStreamWriter osw = new OutputStreamWriter(baos, StandardCharsets.UTF_8);
             CSVWriter writer = new CSVWriter(osw)) {

            // Header row
            writer.writeNext(new String[]{
                    "Session Code",
                    "Student Name",
                    "Student ID",
                    "Vehicle Registration",
                    "Vehicle Type",
                    "Slot Number",
                    "Zone",
                    "Floor",
                    "Entry Time",
                    "Exit Time",
                    "Duration (Minutes)",
                    "Status",
                    "Notes"
            });

            for (ParkingSession s : sessions) {
                String entryStr = s.getEntryTime() != null ? s.getEntryTime().format(FORMATTER) : "";
                String exitStr = s.getExitTime() != null ? s.getExitTime().format(FORMATTER) : "";
                String durationStr = s.getDurationMinutes() != null ? String.valueOf(s.getDurationMinutes()) : "";

                writer.writeNext(new String[]{
                        s.getSessionCode() != null ? s.getSessionCode() : "",
                        s.getUser() != null ? s.getUser().getFullName() : "",
                        s.getUser() != null ? s.getUser().getStudentId() : "",
                        s.getVehicle() != null ? s.getVehicle().getRegistrationNumber() : "",
                        s.getVehicle() != null && s.getVehicle().getVehicleType() != null ? s.getVehicle().getVehicleType().name() : "",
                        s.getSlot() != null ? s.getSlot().getSlotNumber() : "",
                        s.getSlot() != null && s.getSlot().getZone() != null ? s.getSlot().getZone().getName() : "",
                        s.getSlot() != null && s.getSlot().getZone() != null ? s.getSlot().getZone().getFloor() : "",
                        entryStr,
                        exitStr,
                        durationStr,
                        s.getStatus() != null ? s.getStatus().name() : "",
                        s.getNotes() != null ? s.getNotes() : ""
                });
            }

            osw.flush();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate CSV export", e);
        }
    }
}
