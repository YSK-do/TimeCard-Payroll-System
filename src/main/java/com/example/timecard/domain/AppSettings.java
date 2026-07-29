package com.example.timecard.domain;

public record AppSettings(
        String employeeName,
        int hourlyWage,
        int standardWorkMinutes,
        int breakMinutes) {
}
