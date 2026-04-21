package com.example.csvprocessor.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app")
@Data
public class AppProperties {

    private String inputDir = "./input";
    private String outputDir = "./output";
    private String apiBaseUrl = "http://api.zippopotam.us/us";
    private long schedulerFixedRateMs = 120000;
    private String processedExtension = ".processed";
    private String emailFrom = "no-reply@example.com";
}
