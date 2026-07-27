package com.example.timecard.domain;

public record AttendanceResponse(
        long workMinutes,
        long overtimeMinutes,
        String message) {
}