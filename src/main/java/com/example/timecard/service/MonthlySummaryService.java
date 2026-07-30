package com.example.timecard.service;

import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.timecard.domain.AppSettings;
import com.example.timecard.domain.MonthlySummary;
import com.example.timecard.repository.AttendanceCsvRepository;
import com.example.timecard.repository.SettingsCsvRepository;

@Service
public class MonthlySummaryService {

    private final AttendanceCsvRepository attendanceRepository;
    private final SettingsCsvRepository settingsRepository;

    public MonthlySummaryService(
            AttendanceCsvRepository attendanceRepository,
            SettingsCsvRepository settingsRepository) {
        this.attendanceRepository = attendanceRepository;
        this.settingsRepository = settingsRepository;
    }

    public MonthlySummary get(String monthText) {
        YearMonth month = parseMonth(monthText);
        AppSettings settings = loadSettings();

        return attendanceRepository.summarize(
                settings.employeeName(), month);
    }

    public List<String> getRegisteredDates(String monthText) {
        YearMonth month = parseMonth(monthText);
        AppSettings settings = loadSettings();

        return attendanceRepository.findRegisteredDates(
                settings.employeeName(), month);
    }

    private YearMonth parseMonth(String monthText) {
        try {
            return YearMonth.parse(monthText);
        } catch (DateTimeParseException | NullPointerException exception) {
            throw new IllegalArgumentException(
                    "対象月はYYYY-MM形式で指定してください。");
        }
    }

    private AppSettings loadSettings() {
        AppSettings settings = settingsRepository.load();
        if (settings.employeeName().isBlank()) {
            throw new IllegalArgumentException(
                    "先に基本設定を保存してください。");
        }
        return settings;
    }
}
