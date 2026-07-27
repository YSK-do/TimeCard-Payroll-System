package com.example.timecard.domain;

import java.time.LocalDate;
import java.time.LocalTime;

public record AttendanceRequest(
        LocalDate workDate,
        LocalTime startTime,
        LocalTime endTime) {
}