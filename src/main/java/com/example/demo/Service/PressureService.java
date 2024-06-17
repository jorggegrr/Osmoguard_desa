package com.example.demo.Service;


import com.example.demo.model.PressureReading;
import com.example.demo.Repository.PressureReadingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
public class PressureService {

    @Autowired
    private PressureReadingRepository repository;

    private Random random = new Random();

    public PressureReading generateAndSaveReading() {
        PressureReading reading = new PressureReading();
        reading.setTimestamp(LocalDateTime.now());
        reading.setPressure(70 + random.nextDouble() * 30); // Valor de presión entre 70 y 100
        return repository.save(reading);
    }

    @Scheduled(fixedRate = 60000)
    public void generateReadingEveryMinute() {
        generateAndSaveReading();
    }

    public List<PressureReading> getAllReadings() {
        return repository.findAll();
    }

    public List<PressureReading> getReadingsByDateRange(LocalDateTime start, LocalDateTime end) {
        return repository.findAllByTimestampBetween(start, end);
    }

    public void writeReadingsToCsv(PrintWriter writer, LocalDateTime start, LocalDateTime end) {
        List<PressureReading> readings = getReadingsByDateRange(start, end);
        writer.println("Timestamp,Pressure");
        for (PressureReading reading : readings) {
            writer.println(reading.getTimestamp() + "," + reading.getPressure());
        }
    }
    public void writeReadingsToPdf(OutputStream outputStream, LocalDateTime start, LocalDateTime end) throws IOException {
        List<PressureReading> readings = getReadingsByDateRange(start, end);

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                contentStream.beginText();
                contentStream.setLeading(14.5f);
                contentStream.newLineAtOffset(25, 750);
                contentStream.showText("Timestamp, Pressure");
                contentStream.newLine();

                contentStream.setFont(PDType1Font.HELVETICA, 12);
                for (PressureReading reading : readings) {
                    contentStream.showText(reading.getTimestamp() + ", " + reading.getPressure());
                    contentStream.newLine();
                }
                contentStream.endText();
            }

            document.save(outputStream);
        }
    }
}