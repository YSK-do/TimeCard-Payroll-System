package com.example.timecard.domain;

public record AttendanceResponse(
        long workMinutes,
        long overtimeMinutes,
        long basePay,
        long overtimePay,
        long totalPay,
        String message) {
}
