package com.example.timecard.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.example.timecard.domain.AttendanceRecord;

class AttendanceCsvRepositoryTest {

    @TempDir
    Path tempDir;

    private AttendanceRecord record(
            String employeeName,
            LocalDate workDate,
            LocalTime startTime,
            LocalTime endTime) {
        return new AttendanceRecord(
                employeeName,
                workDate,
                startTime,
                endTime,
                480,
                0,
                9600,
                0,
                9600);
    }

    @Test
    void sameEmployeeAndDateIsOverwritten() throws Exception {
        Path csvPath = tempDir.resolve("attendance.csv");
        AttendanceCsvRepository repository =
                new AttendanceCsvRepository(csvPath);

        AttendanceRecord first = record(
                "山田太郎",
                LocalDate.of(2026, 7, 29),
                LocalTime.of(9, 0),
                LocalTime.of(18, 0));

        AttendanceRecord replacement = record(
                "山田太郎",
                LocalDate.of(2026, 7, 29),
                LocalTime.of(10, 0),
                LocalTime.of(19, 0));

        assertThat(repository.save(first)).isFalse();
        assertThat(repository.save(replacement)).isTrue();

        List<String> lines = Files.readAllLines(
                csvPath, StandardCharsets.UTF_8);

        assertThat(lines).containsExactly(
                "employeeName,workDate,startTime,endTime,workMinutes,overtimeMinutes,basePay,overtimePay,totalPay",
                "山田太郎,2026-07-29,10:00,19:00,480,0,9600,0,9600");
    }

    @Test
    void sameDateForDifferentEmployeesIsKept() throws Exception {
        Path csvPath = tempDir.resolve("attendance.csv");
        AttendanceCsvRepository repository =
                new AttendanceCsvRepository(csvPath);

        repository.save(record(
                "山田太郎",
                LocalDate.of(2026, 7, 29),
                LocalTime.of(9, 0),
                LocalTime.of(18, 0)));

        repository.save(record(
                "佐藤花子",
                LocalDate.of(2026, 7, 29),
                LocalTime.of(8, 30),
                LocalTime.of(17, 30)));

        List<String> lines = Files.readAllLines(
                csvPath, StandardCharsets.UTF_8);

        assertThat(lines).hasSize(3);
        assertThat(lines).contains(
                "山田太郎,2026-07-29,09:00,18:00,480,0,9600,0,9600",
                "佐藤花子,2026-07-29,08:30,17:30,480,0,9600,0,9600");
    }

    @Test
    void oldCsvRowsAreMigratedWithoutDataLoss() throws Exception {
        Path csvPath = tempDir.resolve("attendance.csv");
        Files.writeString(
                csvPath,
                "workDate,startTime,endTime,workMinutes,overtimeMinutes\n"
                        + "2026-07-28,09:00,18:00,480,0\n",
                StandardCharsets.UTF_8);

        AttendanceCsvRepository repository =
                new AttendanceCsvRepository(csvPath);

        repository.save(record(
                "山田太郎",
                LocalDate.of(2026, 7, 29),
                LocalTime.of(9, 0),
                LocalTime.of(18, 0)));

        List<String> lines = Files.readAllLines(
                csvPath, StandardCharsets.UTF_8);

        assertThat(lines).containsExactly(
                "employeeName,workDate,startTime,endTime,workMinutes,overtimeMinutes,basePay,overtimePay,totalPay",
                ",2026-07-28,09:00,18:00,480,0,0,0,0",
                "山田太郎,2026-07-29,09:00,18:00,480,0,9600,0,9600");
    }

    @Test
    void summarizesOnlyRequestedEmployeeAndMonth() {
        Path csvPath = tempDir.resolve("attendance.csv");
        AttendanceCsvRepository repository =
                new AttendanceCsvRepository(csvPath);

        repository.save(new AttendanceRecord(
                "山田太郎", LocalDate.of(2026, 7, 1),
                LocalTime.of(9, 0), LocalTime.of(18, 0),
                480, 0, 9600, 0, 9600));
        repository.save(new AttendanceRecord(
                "山田太郎", LocalDate.of(2026, 7, 2),
                LocalTime.of(9, 0), LocalTime.of(19, 0),
                540, 60, 10800, 300, 11100));
        repository.save(new AttendanceRecord(
                "山田太郎", LocalDate.of(2026, 6, 30),
                LocalTime.of(9, 0), LocalTime.of(18, 0),
                480, 0, 9600, 0, 9600));
        repository.save(new AttendanceRecord(
                "佐藤花子", LocalDate.of(2026, 7, 1),
                LocalTime.of(9, 0), LocalTime.of(18, 0),
                480, 0, 9600, 0, 9600));

        var summary = repository.summarize(
                "山田太郎", java.time.YearMonth.of(2026, 7));

        assertThat(summary.workDays()).isEqualTo(2);
        assertThat(summary.workMinutes()).isEqualTo(1020);
        assertThat(summary.overtimeMinutes()).isEqualTo(60);
        assertThat(summary.basePay()).isEqualTo(20400);
        assertThat(summary.overtimePay()).isEqualTo(300);
        assertThat(summary.totalPay()).isEqualTo(20700);
    }
}
