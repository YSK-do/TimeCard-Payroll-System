package com.example.timecard.repository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.example.timecard.domain.AppSettings;

@Repository
public class SettingsCsvRepository {

    private static final Path DEFAULT_CSV_PATH =
            Path.of("data", "settings.csv");

    private static final String HEADER =
            "employeeName,hourlyWage,standardWorkMinutes,breakMinutes";

    private static final AppSettings DEFAULT_SETTINGS =
            new AppSettings("", 1200, 480, 60);

    private final Path csvPath;

    public SettingsCsvRepository() {
        this(DEFAULT_CSV_PATH);
    }

    public SettingsCsvRepository(Path csvPath) {
        this.csvPath = csvPath;
    }

    public synchronized AppSettings load() {
        if (Files.notExists(csvPath)) {
            return DEFAULT_SETTINGS;
        }

        try {
            List<String> lines =
                    Files.readAllLines(csvPath, StandardCharsets.UTF_8);

            if (lines.size() < 2 || lines.get(1).isBlank()) {
                return DEFAULT_SETTINGS;
            }

            String[] values = lines.get(1).split(",", -1);
            if (values.length != 4) {
                throw new IllegalStateException(
                        "設定CSVの形式が不正です。");
            }

            return new AppSettings(
                    values[0],
                    Integer.parseInt(values[1]),
                    Integer.parseInt(values[2]),
                    Integer.parseInt(values[3]));
        } catch (IOException | NumberFormatException exception) {
            throw new IllegalStateException(
                    "設定CSVの読み込みに失敗しました。",
                    exception);
        }
    }

    public synchronized void save(AppSettings settings) {
        try {
            Path parent = csvPath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }

            String row = String.join(",",
                    settings.employeeName(),
                    Integer.toString(settings.hourlyWage()),
                    Integer.toString(settings.standardWorkMinutes()),
                    Integer.toString(settings.breakMinutes()));

            Files.write(
                    csvPath,
                    List.of(HEADER, row),
                    StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "設定CSVの保存に失敗しました。",
                    exception);
        }
    }
}
