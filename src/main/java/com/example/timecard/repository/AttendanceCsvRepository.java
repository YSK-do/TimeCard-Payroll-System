package com.example.timecard.repository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import org.springframework.stereotype.Repository;

import com.example.timecard.domain.AttendanceRecord;

@Repository
public class AttendanceCsvRepository {

    private static final Path CSV_PATH =
            Path.of("data", "attendance.csv");

    private static final String HEADER =
            "workDate,startTime,endTime,workMinutes,overtimeMinutes";

    public synchronized void save(AttendanceRecord record) {
        try {
            Files.createDirectories(CSV_PATH.getParent());

            if (Files.notExists(CSV_PATH)) {
                Files.writeString(
                        CSV_PATH,
                        HEADER + System.lineSeparator(),
                        StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE);
            }

            String row = String.join(",",
                    record.workDate().toString(),
                    record.startTime().toString(),
                    record.endTime().toString(),
                    Long.toString(record.workMinutes()),
                    Long.toString(record.overtimeMinutes()));

            Files.writeString(
                    CSV_PATH,
                    row + System.lineSeparator(),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.APPEND);

        } catch (IOException exception) {
            throw new IllegalStateException(
                    "勤怠CSVの保存に失敗しました。",
                    exception);
        }
    }
}