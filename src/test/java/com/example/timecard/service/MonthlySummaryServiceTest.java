package com.example.timecard.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.YearMonth;

import org.junit.jupiter.api.Test;

import com.example.timecard.domain.AppSettings;
import com.example.timecard.domain.MonthlySummary;
import com.example.timecard.repository.AttendanceCsvRepository;
import com.example.timecard.repository.SettingsCsvRepository;

class MonthlySummaryServiceTest {

    @Test
    void usesSavedEmployeeForRequestedMonth() {
        AttendanceCsvRepository attendanceRepository =
                new AttendanceCsvRepository() {
                    @Override
                    public MonthlySummary summarize(
                            String employeeName, YearMonth month) {
                        assertThat(employeeName).isEqualTo("山田太郎");
                        assertThat(month).isEqualTo(
                                YearMonth.of(2026, 7));
                        return new MonthlySummary(
                                "2026-07", 2, 960, 60,
                                19200, 300, 19500);
                    }
                };
        SettingsCsvRepository settingsRepository =
                new SettingsCsvRepository() {
                    @Override
                    public AppSettings load() {
                        return new AppSettings(
                                "山田太郎", 1200, 480, 60);
                    }
                };

        MonthlySummaryService service =
                new MonthlySummaryService(
                        attendanceRepository,
                        settingsRepository);

        assertThat(service.get("2026-07").workDays())
                .isEqualTo(2);
    }

    @Test
    void rejectsInvalidMonth() {
        MonthlySummaryService service =
                new MonthlySummaryService(
                        new AttendanceCsvRepository(),
                        new SettingsCsvRepository());

        assertThatThrownBy(() -> service.get("2026/07"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("対象月はYYYY-MM形式で指定してください。");
    }
}
