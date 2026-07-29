package com.example.timecard.domain;

import java.time.LocalDate;
import java.time.LocalTime;

public record AttendanceRequest(
        String employeeName,
        LocalDate workDate,
        LocalTime startTime,
        LocalTime endTime) {
}
