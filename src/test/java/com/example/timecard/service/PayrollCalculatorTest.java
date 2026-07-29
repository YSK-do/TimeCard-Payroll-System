package com.example.timecard.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.example.timecard.domain.PayrollCalculation;

class PayrollCalculatorTest {

    private final PayrollCalculator calculator =
            new PayrollCalculator();

    @Test
    void calculatesBasePayOvertimePayAndTotalPay() {
        PayrollCalculation result =
                calculator.calculate(510, 30, 1200);

        assertThat(result.basePay()).isEqualTo(10200);
        assertThat(result.overtimePay()).isEqualTo(150);
        assertThat(result.totalPay()).isEqualTo(10350);
    }

    @Test
    void truncatesFractionsSmallerThanOneYen() {
        PayrollCalculation result =
                calculator.calculate(1, 1, 1000);

        assertThat(result.basePay()).isEqualTo(16);
        assertThat(result.overtimePay()).isEqualTo(4);
        assertThat(result.totalPay()).isEqualTo(20);
    }

    @Test
    void rejectsInvalidValues() {
        assertThatThrownBy(
                () -> calculator.calculate(30, 31, 1200))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
