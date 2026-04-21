package com.example.csvprocessor.service;

import com.example.csvprocessor.client.ZippopotamusClient;
import com.example.csvprocessor.config.AppProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CsvProcessingServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void processesCsvFileAndRenamesInput() throws Exception {
        AppProperties properties = new AppProperties();
        properties.setInputDir(tempDir.resolve("input").toString());
        properties.setOutputDir(tempDir.resolve("output").toString());
        properties.setProcessedExtension(".processed");

        Path inputDirectory = tempDir.resolve("input");
        Files.createDirectories(inputDirectory);
        Path inputFile = inputDirectory.resolve("sample.csv");
        Files.writeString(inputFile, "zip_code,email\n90210,test@example.com\n");

        ZippopotamusClient client = Mockito.mock(ZippopotamusClient.class);
        when(client.resolveState("90210")).thenReturn("California");

        EmailNotificationService emailService = Mockito.mock(EmailNotificationService.class);
        CsvProcessingService service = new CsvProcessingService(properties, client, emailService);

        service.processPendingFiles();

        Path outputFile = tempDir.resolve("output").resolve("sample.csv");
        Path processedFile = inputDirectory.resolve("sample.csv.processed");

        assertTrue(Files.exists(outputFile));
        assertTrue(Files.exists(processedFile));
        verify(emailService, times(1)).sendNotification("test@example.com", "90210", "California");
    }
}
