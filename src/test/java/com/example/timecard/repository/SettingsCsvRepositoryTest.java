package com.example.timecard.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.example.timecard.domain.AppSettings;

class SettingsCsvRepositoryTest {

    @TempDir
    Path tempDir;

    @Test
    void returnsDefaultsWhenFileDoesNotExist() {
        SettingsCsvRepository repository =
                new SettingsCsvRepository(
                        tempDir.resolve("settings.csv"));

        AppSettings settings = repository.load();

        assertThat(settings.employeeName()).isEmpty();
        assertThat(settings.hourlyWage()).isEqualTo(1200);
        assertThat(settings.standardWorkMinutes()).isEqualTo(480);
        assertThat(settings.breakMinutes()).isEqualTo(60);
    }

    @Test
    void savesAndLoadsSettings() {
        SettingsCsvRepository repository =
                new SettingsCsvRepository(
                        tempDir.resolve("settings.csv"));

        AppSettings expected =
                new AppSettings("山田太郎", 1350, 450, 45);

        repository.save(expected);

        assertThat(repository.load()).isEqualTo(expected);
    }
}
