package com.example.timecard.service;

import org.springframework.stereotype.Service;

import com.example.timecard.domain.PayrollCalculation;

@Service
public class PayrollCalculator {

    private static final int MINUTES_PER_HOUR = 60;
    private static final int OVERTIME_RATE_PERCENT = 25;

    public PayrollCalculation calculate(
            long workMinutes,
            long overtimeMinutes,
            int hourlyWage) {
        if (workMinutes < 0
                || overtimeMinutes < 0
                || overtimeMinutes > workMinutes
                || hourlyWage <= 0) {
            throw new IllegalArgumentException(
                    "給与計算に使用する値が正しくありません。");
        }

        long basePay =
                workMinutes * hourlyWage / MINUTES_PER_HOUR;
        long overtimePay =
                overtimeMinutes * hourlyWage
                * OVERTIME_RATE_PERCENT
                / (MINUTES_PER_HOUR * 100);
        long totalPay = basePay + overtimePay;

        return new PayrollCalculation(
                basePay,
                overtimePay,
                totalPay);
    }
}
