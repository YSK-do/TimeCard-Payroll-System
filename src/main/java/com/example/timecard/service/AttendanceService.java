package com.example.timecard.service;

import java.time.Duration;

import org.springframework.stereotype.Service;

import com.example.timecard.domain.AttendanceRecord;
import com.example.timecard.domain.AttendanceRequest;
import com.example.timecard.domain.AttendanceResponse;
import com.example.timecard.repository.AttendanceCsvRepository;

@Service
public class AttendanceService {

    private static final long BREAK_MINUTES = 60;
    private static final long STANDARD_WORK_MINUTES = 8 * 60;

    private final AttendanceCsvRepository repository;

    public AttendanceService(AttendanceCsvRepository repository) {
        this.repository = repository;
    }

    public AttendanceResponse register(AttendanceRequest request) {
        if (request.workDate() == null
                || request.startTime() == null
                || request.endTime() == null) {
            throw new IllegalArgumentException(
                    "勤務日と出退勤時刻を入力してください。");
        }

        long elapsedMinutes = Duration.between(
                request.startTime(),
                request.endTime()).toMinutes();

        if (elapsedMinutes <= 0) {
            throw new IllegalArgumentException(
                    "退勤時刻は出勤時刻より後にしてください。");
        }

        long workMinutes = elapsedMinutes - BREAK_MINUTES;

        if (workMinutes < 0) {
            throw new IllegalArgumentException(
                    "勤務時間は休憩時間より長くしてください。");
        }

        long overtimeMinutes =
                Math.max(0, workMinutes - STANDARD_WORK_MINUTES);

        AttendanceRecord record = new AttendanceRecord(
                request.workDate(),
                request.startTime(),
                request.endTime(),
                workMinutes,
                overtimeMinutes);

        repository.save(record);

        return new AttendanceResponse(
                workMinutes,
                overtimeMinutes,
                "勤怠をCSVへ保存しました。");
    }
}