package com.example.demo.controller;


import com.example.demo.model.PressureReading;
import com.example.demo.Service.PressureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/pressure")
public class PressureController {

    @Autowired
    private PressureService service;

    @GetMapping("/generate")
    public PressureReading generateReading() {
        return service.generateAndSaveReading();
    }

    @GetMapping("/readings")
    public List<PressureReading> getAllReadings() {
        return service.getAllReadings();
    }

    @GetMapping("/readings/filter")
    public List<PressureReading> getReadingsByDateRange(
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return service.getReadingsByDateRange(start, end);
    }

    @GetMapping("/readings/export")
    public void exportReadingsToCsv(
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"readings.csv\"");
        service.writeReadingsToCsv(response.getWriter(), start, end);
    }
    @GetMapping("/readings/export/pdf")
    public void exportReadingsToPdf(
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            HttpServletResponse response) throws IOException {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=\"readings.pdf\"");
        service.writeReadingsToPdf(response.getOutputStream(), start, end);
    }

}