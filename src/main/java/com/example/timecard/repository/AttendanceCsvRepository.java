package com.example.timecard.repository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.example.timecard.domain.AttendanceRecord;

@Repository
public class AttendanceCsvRepository {

    private static final Path DEFAULT_CSV_PATH =
            Path.of("data", "attendance.csv");

    private static final String HEADER =
            "employeeName,workDate,startTime,endTime,workMinutes,overtimeMinutes";

    private static final String OLD_HEADER =
            "workDate,startTime,endTime,workMinutes,overtimeMinutes";

    private final Path csvPath;

    public AttendanceCsvRepository() {
        this(DEFAULT_CSV_PATH);
    }

    AttendanceCsvRepository(Path csvPath) {
        this.csvPath = csvPath;
    }

    public synchronized boolean save(AttendanceRecord record) {
        try {
            Path parent = csvPath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }

            List<String> rows = new ArrayList<>();
            rows.add(HEADER);

            boolean updated = false;

            if (Files.exists(csvPath)) {
                for (String line : Files.readAllLines(
                        csvPath, StandardCharsets.UTF_8)) {
                    if (line.isBlank()
                            || line.equals(HEADER)
                            || line.equals(OLD_HEADER)) {
                        continue;
                    }

                    String migratedLine = migrateOldRow(line);
                    String[] values = migratedLine.split(",", -1);

                    boolean sameEmployeeAndDate =
                            values.length == 6
                            && values[0].equals(record.employeeName())
                            && values[1].equals(
                                    record.workDate().toString());

                    if (sameEmployeeAndDate) {
                        updated = true;
                    } else {
                        rows.add(migratedLine);
                    }
                }
            }

            rows.add(toCsvRow(record));

            Files.write(csvPath, rows, StandardCharsets.UTF_8);
            return updated;

        } catch (IOException exception) {
            throw new IllegalStateException(
                    "勤怠CSVの保存に失敗しました。",
                    exception);
        }
    }

    private String migrateOldRow(String line) {
        String[] values = line.split(",", -1);
        if (values.length == 5) {
            return "," + line;
        }
        return line;
    }

    private String toCsvRow(AttendanceRecord record) {
        return String.join(",",
                record.employeeName(),
                record.workDate().toString(),
                record.startTime().toString(),
                record.endTime().toString(),
                Long.toString(record.workMinutes()),
                Long.toString(record.overtimeMinutes()));
    }
}
