package com.example.timecard.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.timecard.domain.AttendanceRecord;
import com.example.timecard.domain.AttendanceRequest;
import com.example.timecard.domain.AttendanceResponse;
import com.example.timecard.domain.MonthlySummary;
import com.example.timecard.service.AttendanceService;
import com.example.timecard.service.MonthlySummaryService;

@RestController
@RequestMapping("/api/attendances")
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final MonthlySummaryService monthlySummaryService;

    public AttendanceController(
            AttendanceService attendanceService,
            MonthlySummaryService monthlySummaryService) {
        this.attendanceService = attendanceService;
        this.monthlySummaryService = monthlySummaryService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AttendanceResponse create(
            @RequestBody AttendanceRequest request) {
        return attendanceService.register(request);
    }

    @GetMapping("/summary")
    public MonthlySummary summary(
            @RequestParam String month) {
        return monthlySummaryService.get(month);
    }

    @GetMapping("/dates")
    public List<String> registeredDates(
            @RequestParam String month) {
        return monthlySummaryService.getRegisteredDates(month);
    }

    @GetMapping("/detail")
    public AttendanceRecord detail(
            @RequestParam String date) {
        return monthlySummaryService.getByDate(date);
    }
}
