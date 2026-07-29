package com.example.timecard.domain;

import java.time.LocalDate;
import java.time.LocalTime;

public record AttendanceRecord(
        String employeeName,
        LocalDate workDate,
        LocalTime startTime,
        LocalTime endTime,
        long workMinutes,
        long overtimeMinutes,
        long basePay,
        long overtimePay,
        long totalPay) {
}
