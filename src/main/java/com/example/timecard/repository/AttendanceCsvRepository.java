package com.example.timecard.repository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.example.timecard.domain.AttendanceRecord;
import com.example.timecard.domain.MonthlySummary;

@Repository
public class AttendanceCsvRepository {

    private static final Path DEFAULT_CSV_PATH =
            Path.of("data", "attendance.csv");

    private static final String HEADER =
            "employeeName,workDate,startTime,endTime,workMinutes,overtimeMinutes,basePay,overtimePay,totalPay";

    private static final String NAME_HEADER =
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
                    if (isHeaderOrBlank(line)) {
                        continue;
                    }

                    String migratedLine = migrateOldRow(line);
                    String[] values = migratedLine.split(",", -1);

                    boolean sameEmployeeAndDate =
                            values.length == 9
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

    public synchronized MonthlySummary summarize(
            String employeeName, YearMonth month) {
        int workDays = 0;
        long workMinutes = 0;
        long overtimeMinutes = 0;
        long basePay = 0;
        long overtimePay = 0;
        long totalPay = 0;

        if (!Files.exists(csvPath)) {
            return new MonthlySummary(
                    month.toString(), 0, 0, 0, 0, 0, 0);
        }

        try {
            for (String line : Files.readAllLines(
                    csvPath, StandardCharsets.UTF_8)) {
                if (isHeaderOrBlank(line)) {
                    continue;
                }

                String[] values = migrateOldRow(line).split(",", -1);
                if (values.length != 9
                        || !values[0].equals(employeeName)) {
                    continue;
                }

                LocalDate workDate = LocalDate.parse(values[1]);
                if (!YearMonth.from(workDate).equals(month)) {
                    continue;
                }

                workDays++;
                workMinutes += Long.parseLong(values[4]);
                overtimeMinutes += Long.parseLong(values[5]);
                basePay += Long.parseLong(values[6]);
                overtimePay += Long.parseLong(values[7]);
                totalPay += Long.parseLong(values[8]);
            }
        } catch (IOException | RuntimeException exception) {
            throw new IllegalStateException(
                    "勤怠CSVの月間集計に失敗しました。",
                    exception);
        }

        return new MonthlySummary(
                month.toString(),
                workDays,
                workMinutes,
                overtimeMinutes,
                basePay,
                overtimePay,
                totalPay);
    }

    public synchronized List<String> findRegisteredDates(
            String employeeName, YearMonth month) {
        List<String> registeredDates = new ArrayList<>();

        if (!Files.exists(csvPath)) {
            return registeredDates;
        }

        try {
            for (String line : Files.readAllLines(
                    csvPath, StandardCharsets.UTF_8)) {
                if (isHeaderOrBlank(line)) {
                    continue;
                }

                String[] values = migrateOldRow(line).split(",", -1);
                if (values.length != 9
                        || !values[0].equals(employeeName)) {
                    continue;
                }

                LocalDate workDate = LocalDate.parse(values[1]);
                if (YearMonth.from(workDate).equals(month)) {
                    registeredDates.add(workDate.toString());
                }
            }
        } catch (IOException | RuntimeException exception) {
            throw new IllegalStateException(
                    "登録済み勤務日の読み込みに失敗しました。",
                    exception);
        }

        return registeredDates;
    }

    public synchronized Optional<AttendanceRecord> findByDate(
            String employeeName, LocalDate workDate) {
        if (!Files.exists(csvPath)) {
            return Optional.empty();
        }

        try {
            for (String line : Files.readAllLines(
                    csvPath, StandardCharsets.UTF_8)) {
                if (isHeaderOrBlank(line)) {
                    continue;
                }

                String[] values = migrateOldRow(line).split(",", -1);
                if (values.length == 9
                        && values[0].equals(employeeName)
                        && values[1].equals(workDate.toString())) {
                    return Optional.of(new AttendanceRecord(
                            values[0],
                            LocalDate.parse(values[1]),
                            LocalTime.parse(values[2]),
                            LocalTime.parse(values[3]),
                            Long.parseLong(values[4]),
                            Long.parseLong(values[5]),
                            Long.parseLong(values[6]),
                            Long.parseLong(values[7]),
                            Long.parseLong(values[8])));
                }
            }
        } catch (IOException | RuntimeException exception) {
            throw new IllegalStateException(
                    "選択日の勤怠読み込みに失敗しました。",
                    exception);
        }

        return Optional.empty();
    }

    private boolean isHeaderOrBlank(String line) {
        return line.isBlank()
                || line.equals(HEADER)
                || line.equals(NAME_HEADER)
                || line.equals(OLD_HEADER);
    }

    private String migrateOldRow(String line) {
        String[] values = line.split(",", -1);
        if (values.length == 5) {
            return "," + line + ",0,0,0";
        }
        if (values.length == 6) {
            return line + ",0,0,0";
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
                Long.toString(record.overtimeMinutes()),
                Long.toString(record.basePay()),
                Long.toString(record.overtimePay()),
                Long.toString(record.totalPay()));
    }
}
