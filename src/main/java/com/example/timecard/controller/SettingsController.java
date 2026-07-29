package com.example.timecard.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.timecard.domain.AppSettings;
import com.example.timecard.service.SettingsService;

@RestController
@RequestMapping("/api/settings")
public class SettingsController {

    private final SettingsService settingsService;

    public SettingsController(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @GetMapping
    public AppSettings get() {
        return settingsService.get();
    }

    @PutMapping
    public AppSettings update(@RequestBody AppSettings settings) {
        return settingsService.update(settings);
    }
}
