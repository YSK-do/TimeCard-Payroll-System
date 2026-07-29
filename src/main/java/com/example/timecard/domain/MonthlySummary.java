package com.example.timecard.domain;

public record MonthlySummary(
        String month,
        int workDays,
        long workMinutes,
        long overtimeMinutes,
        long basePay,
        long overtimePay,
        long totalPay) {
}
