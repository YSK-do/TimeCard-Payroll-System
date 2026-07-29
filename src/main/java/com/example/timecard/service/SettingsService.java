package com.example.timecard.service;

import org.springframework.stereotype.Service;

import com.example.timecard.domain.AppSettings;
import com.example.timecard.repository.SettingsCsvRepository;

@Service
public class SettingsService {

    private final SettingsCsvRepository repository;

    public SettingsService(SettingsCsvRepository repository) {
        this.repository = repository;
    }

    public AppSettings get() {
        return repository.load();
    }

    public AppSettings update(AppSettings settings) {
        validate(settings);

        AppSettings normalized = new AppSettings(
                settings.employeeName().trim(),
                settings.hourlyWage(),
                settings.standardWorkMinutes(),
                settings.breakMinutes());

        repository.save(normalized);
        return normalized;
    }

    private void validate(AppSettings settings) {
        if (settings == null
                || settings.employeeName() == null
                || settings.employeeName().isBlank()) {
            throw new IllegalArgumentException(
                    "氏名を入力してください。");
        }

        if (settings.employeeName().contains(",")) {
            throw new IllegalArgumentException(
                    "氏名にカンマは使用できません。");
        }

        if (settings.hourlyWage() <= 0) {
            throw new IllegalArgumentException(
                    "時給は1円以上で入力してください。");
        }

        if (settings.standardWorkMinutes() <= 0
                || settings.standardWorkMinutes() > 24 * 60) {
            throw new IllegalArgumentException(
                    "標準労働時間は1分から24時間の範囲で入力してください。");
        }

        if (settings.breakMinutes() < 0
                || settings.breakMinutes() >= 24 * 60) {
            throw new IllegalArgumentException(
                    "休憩時間は0分から24時間未満で入力してください。");
        }
    }
}
