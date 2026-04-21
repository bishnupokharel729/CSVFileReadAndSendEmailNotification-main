package com.example.csvprocessor.scheduler;

import com.example.csvprocessor.service.CsvProcessingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CsvFileScheduler {

    private final CsvProcessingService csvProcessingService;

    public CsvFileScheduler(CsvProcessingService csvProcessingService) {
        this.csvProcessingService = csvProcessingService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("Application started. Performing initial input directory scan.");
        csvProcessingService.processPendingFiles();
    }

    @Scheduled(fixedRateString = "${app.scheduler.fixed-rate-ms}", initialDelayString = "30000")
    public void scanInputDirectory() {
        log.info("Starting scheduled scan of input directory");
        csvProcessingService.processPendingFiles();
    }
}
