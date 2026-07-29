package com.example.timecard.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.example.timecard.domain.AppSettings;
import com.example.timecard.repository.SettingsCsvRepository;

class SettingsServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void trimsEmployeeNameAndSavesSettings() {
        SettingsCsvRepository repository =
                new SettingsCsvRepository(
                        tempDir.resolve("settings.csv"));
        SettingsService service = new SettingsService(repository);

        AppSettings saved = service.update(
                new AppSettings(" 山田太郎 ", 1300, 480, 60));

        assertThat(saved.employeeName()).isEqualTo("山田太郎");
        assertThat(repository.load()).isEqualTo(saved);
    }

    @Test
    void rejectsInvalidValues() {
        SettingsService service = new SettingsService(
                new SettingsCsvRepository(
                        tempDir.resolve("settings.csv")));

        assertThatThrownBy(() -> service.update(
                new AppSettings("", 0, 0, -1)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
