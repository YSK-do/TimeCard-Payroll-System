package com.example.timecard.service;

import java.time.Duration;

import org.springframework.stereotype.Service;

import com.example.timecard.domain.AppSettings;
import com.example.timecard.domain.AttendanceRecord;
import com.example.timecard.domain.AttendanceRequest;
import com.example.timecard.domain.AttendanceResponse;
import com.example.timecard.domain.PayrollCalculation;
import com.example.timecard.repository.AttendanceCsvRepository;
import com.example.timecard.repository.SettingsCsvRepository;

@Service
public class AttendanceService {

    private final AttendanceCsvRepository repository;
    private final SettingsCsvRepository settingsRepository;
    private final PayrollCalculator payrollCalculator;

    public AttendanceService(
            AttendanceCsvRepository repository,
            SettingsCsvRepository settingsRepository,
            PayrollCalculator payrollCalculator) {
        this.repository = repository;
        this.settingsRepository = settingsRepository;
        this.payrollCalculator = payrollCalculator;
    }

    public AttendanceResponse register(AttendanceRequest request) {
        if (request.workDate() == null
                || request.startTime() == null
                || request.endTime() == null) {
            throw new IllegalArgumentException(
                    "勤務日と出退勤時刻を入力してください。");
        }

        AppSettings settings = settingsRepository.load();

        if (settings.employeeName().isBlank()) {
            throw new IllegalArgumentException(
                    "先に基本設定を保存してください。");
        }

        long elapsedMinutes = Duration.between(
                request.startTime(),
                request.endTime()).toMinutes();

        if (elapsedMinutes <= 0) {
            throw new IllegalArgumentException(
                    "退勤時刻は出勤時刻より後にしてください。");
        }

        long workMinutes =
                elapsedMinutes - settings.breakMinutes();

        if (workMinutes < 0) {
            throw new IllegalArgumentException(
                    "勤務時間は休憩時間より長くしてください。");
        }

        long overtimeMinutes = Math.max(
                0,
                workMinutes - settings.standardWorkMinutes());

        PayrollCalculation payroll = payrollCalculator.calculate(
                workMinutes,
                overtimeMinutes,
                settings.hourlyWage());

        AttendanceRecord record = new AttendanceRecord(
                settings.employeeName(),
                request.workDate(),
                request.startTime(),
                request.endTime(),
                workMinutes,
                overtimeMinutes,
                payroll.basePay(),
                payroll.overtimePay(),
                payroll.totalPay());

        boolean updated = repository.save(record);

        String message = updated
                ? "同じ勤務日・氏名の勤怠を上書きしました。"
                : "勤怠をCSVへ保存しました。";

        return new AttendanceResponse(
                workMinutes,
                overtimeMinutes,
                payroll.basePay(),
                payroll.overtimePay(),
                payroll.totalPay(),
                message);
    }
}
