package com.example.csvprocessor.service;

import com.example.csvprocessor.client.ZippopotamusClient;
import com.example.csvprocessor.config.AppProperties;
import com.example.csvprocessor.model.CsvRecord;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Slf4j
public class CsvProcessingService {

    private static final List<String> EXPECTED_HEADERS = List.of("zip_code", "email");

    private final AppProperties appProperties;
    private final ZippopotamusClient zippopotamusClient;
    private final EmailNotificationService emailNotificationService;

    public CsvProcessingService(AppProperties appProperties,
                                ZippopotamusClient zippopotamusClient,
                                EmailNotificationService emailNotificationService) {
        this.appProperties = appProperties;
        this.zippopotamusClient = zippopotamusClient;
        this.emailNotificationService = emailNotificationService;
    }

    public void processPendingFiles() {
        Path inputDirectory = resolveDirectory(Path.of(appProperties.getInputDir()));
        Path outputDirectory = resolveDirectory(Path.of(appProperties.getOutputDir()));

        log.info("Using input directory: {}", inputDirectory.toAbsolutePath());
        log.info("Using output directory: {}", outputDirectory.toAbsolutePath());

        try {
            Files.createDirectories(inputDirectory);
            Files.createDirectories(outputDirectory);
        } catch (IOException e) {
            log.error("Unable to create input/output directories: {}", e.getMessage(), e);
            return;
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(inputDirectory, path -> {
            String fileName = path.getFileName().toString().toLowerCase();
            return Files.isRegularFile(path) && fileName.endsWith(".csv") && !fileName.endsWith(appProperties.getProcessedExtension());
        })) {
            for (Path csvFile : stream) {
                processCsvFile(csvFile, outputDirectory);
            }
        } catch (IOException e) {
            log.error("Failed to scan input directory {}: {}", inputDirectory, e.getMessage(), e);
        }
    }

    private void processCsvFile(Path csvFile, Path outputDirectory) {
        log.info("Processing CSV file {}", csvFile.getFileName());
        Path outputFile = outputDirectory.resolve(buildOutputFileName(csvFile.getFileName().toString()));
        Path processedFile = csvFile.resolveSibling(csvFile.getFileName().toString() + appProperties.getProcessedExtension());
        System.out.println("**************Processing file: " + csvFile.toAbsolutePath());
        var csvFormat = CSVFormat.DEFAULT.builder()
                .setHeader("zip_code", "email")
                .setSkipHeaderRecord(true)
                .setIgnoreHeaderCase(true)
                .setTrim(true)
                .build();

        try (BufferedReader reader = Files.newBufferedReader(csvFile);
             CSVParser parser = CSVParser.parse(reader, csvFormat);
             BufferedWriter writer = Files.newBufferedWriter(outputFile, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
             CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT.builder().setHeader("zip_code", "email", "state").build())) {

            validateHeader(parser.getHeaderNames());

            for (CSVRecord csvRecord : parser) {
                processCsvRecord(csvRecord, printer);
            }

            log.info("Successfully produced output file {}", outputFile.getFileName());
        } catch (IOException e) {
            log.error("Error processing file {}: {}", csvFile.getFileName(), e.getMessage(), e);
        } finally {
            renameProcessedFile(csvFile, processedFile);
        }
    }

    private void validateHeader(List<String> headerNames) {
        if (headerNames == null || !headerNames.containsAll(EXPECTED_HEADERS)) {
            throw new IllegalArgumentException("CSV header must contain zip_code and email columns");
        }
    }

    private void processCsvRecord(CSVRecord csvRecord, CSVPrinter printer) {
        try {
            String zipCode = csvRecord.get("zip_code").trim();
            String email = csvRecord.get("email").trim();
            String state = zippopotamusClient.resolveState(zipCode);
            CsvRecord record = CsvRecord.builder()
                    .zipCode(zipCode)
                    .email(email)
                    .state(state)
                    .build();

            printer.printRecord(record.getZipCode(), record.getEmail(), record.getState());
                    System.out.println("************Processed record: " + record);
            if (StringUtils.hasText(email)) {
                emailNotificationService.sendNotification(email, zipCode, state);
            }
        } catch (Exception e) {
            log.warn("Skipping invalid row {}: {}", csvRecord.getRecordNumber(), e.getMessage());
        }
    }

    private void renameProcessedFile(Path originalFile, Path processedFile) {
        try {
            Files.move(originalFile, processedFile);
            log.info("Renamed processed file to {}", processedFile.getFileName());
        } catch (IOException e) {
            log.error("Failed to rename processed file {}: {}", originalFile.getFileName(), e.getMessage(), e);
        }
    }

    private String buildOutputFileName(String originalFileName) {
        String baseName = originalFileName;
        String extension = ".csv";
        int dotIndex = originalFileName.lastIndexOf('.');
        if (dotIndex >= 0) {
            baseName = originalFileName.substring(0, dotIndex);
            extension = originalFileName.substring(dotIndex);
        }
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return String.format("%s-output-%s%s", baseName, timestamp, extension);
    }

    private Path resolveDirectory(Path configuredDir) {
        if (configuredDir.isAbsolute()) {
            return configuredDir;
        }
        return Path.of(System.getProperty("user.dir")).resolve(configuredDir).normalize();
    }
}
