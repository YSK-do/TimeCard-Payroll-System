package com.example.timecard.domain;

public record PayrollCalculation(
        long basePay,
        long overtimePay,
        long totalPay) {
}
