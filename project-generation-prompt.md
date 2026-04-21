# CSV File Processor - Project Generation Prompt

**Create a complete Spring Boot application that processes CSV files containing ZIP codes and email addresses. The application should:**

1. **Monitor an input directory** for CSV files every 2 minutes using a scheduled task
2. **Process CSV files** with headers `zip_code,email` and resolve US states using the Zippopotam.us API (`http://api.zippopotam.us/us/{zip}`)
3. **Generate output files** in the format `inputfilename-output-YYYYMMDDHHMMSS.csv` containing `zip_code,email,state` columns
4. **Send email notifications** for each processed record using Spring Boot Mail
5. **Rename processed input files** with `.processed` extension
6. **Include comprehensive error handling** and logging
7. **Provide REST API endpoints** with Swagger/OpenAPI documentation
8. **Include unit tests** for the API client and CSV processing service
9. **Support Docker containerization**

**Technical Requirements:**
- Java 21 with Spring Boot 3.x
- Maven build system
- Dependencies: Spring Web, Spring Mail, Apache Commons CSV, Lombok, SLF4J
- Clean architecture with service layers
- Reactive WebClient for API calls
- Proper configuration management

**Project Structure:**
```
src/main/java/com/example/csvprocessor/
├── CsvFileReadAndSendEmailApplication.java
├── client/ZippopotamusClient.java
├── config/
│   ├── AppConfig.java
│   └── AppProperties.java
├── model/
│   ├── CsvRecord.java
│   └── ZippopotamusResponse.java
├── scheduler/CsvFileScheduler.java
├── service/
│   ├── CsvProcessingService.java
│   └── EmailNotificationService.java
└── web/StatusController.java

src/test/java/com/example/csvprocessor/
├── client/ZippopotamusClientTest.java
└── service/CsvProcessingServiceTest.java

src/main/resources/
└── application.properties

Dockerfile
pom.xml
README.md
```

**Configuration (application.properties):**
- Gmail SMTP setup with App Password authentication
- Configurable input/output directories
- API base URL and scheduler settings

**Key Features:**
- Graceful handling of invalid ZIP codes (fallback to "UNKNOWN")
- Skip email notifications for empty email addresses
- Comprehensive logging with timestamps
- Docker containerization support

**Generate the complete project with all source files, configuration, build files, and documentation.**

## Detailed Implementation Requirements

### Main Application Class
- Standard Spring Boot main class with `@SpringBootApplication`
- Enable scheduling with `@EnableScheduling`
- Package: `com.example.csvprocessor`

### Configuration Classes

**AppProperties.java:**
- Use `@ConfigurationProperties(prefix = "app")`
- Fields: inputDir, outputDir, apiBaseUrl, schedulerFixedRateMs, processedExtension, emailFrom
- Include validation annotations

**AppConfig.java:**
- Configure WebClient bean for Zippopotam.us API
- Base URL from AppProperties

### Model Classes

**CsvRecord.java:**
- Lombok `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`
- Fields: zipCode, email, state (all String)

**ZippopotamusResponse.java:**
- Fields: country, countryAbbreviation, places (List of Place objects)
- Place class with placeName, longitude, latitude, state, stateAbbreviation

### Service Classes

**CsvProcessingService.java:**
- Process pending files from input directory
- Validate CSV headers (zip_code, email)
- For each row: resolve state via API, write to output CSV, send email
- Output filename format: `inputfilename-output-YYYYMMDDHHMMSS.csv`
- Rename processed files with `.processed` extension
- Comprehensive error handling and logging

**EmailNotificationService.java:**
- Send notification emails using JavaMailSender
- Skip if email is empty/null
- Email subject: "ZIP Code Processing Result"
- Include ZIP code, resolved state, and thank you message

### Client Class

**ZippopotamusClient.java:**
- WebClient-based API client
- Method: `String resolveState(String zipCode)`
- Handle API errors gracefully (return "UNKNOWN" on failure)
- Parse JSON response to extract state

### Scheduler Class

**CsvFileScheduler.java:**
- `@Scheduled(fixedRateString = "${app.scheduler.fixed-rate-ms}")`
- Initial scan on application ready event
- Call CsvProcessingService.processPendingFiles()

### Web Controller

**StatusController.java:**
- REST endpoint: `GET /api/status`
- Return simple status message
- Include Swagger annotations

### Unit Tests

**ZippopotamusClientTest.java:**
- Mock WebClient responses
- Test successful state resolution
- Test error handling (invalid ZIP, API errors)

**CsvProcessingServiceTest.java:**
- Mock dependencies (client, email service)
- Test CSV processing logic
- Test file operations
- Test error scenarios

### Build Configuration (pom.xml)

**Dependencies:**
- spring-boot-starter-web
- spring-boot-starter-mail
- spring-boot-starter-validation
- commons-csv
- lombok
- spring-boot-starter-test
- springdoc-openapi-starter-webmvc-ui

**Build Plugins:**
- spring-boot-maven-plugin
- maven-compiler-plugin (Java 21)

### Application Properties

```properties
# Gmail SMTP Configuration
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-gmail@gmail.com
spring.mail.password=YOUR_GMAIL_APP_PASSWORD
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true
spring.mail.from=your-gmail@gmail.com

# Application Configuration
app.input-dir=./input
app.output-dir=./output
app.api.base-url=http://api.zippopotam.us/us
app.scheduler.fixed-rate-ms=120000
app.processed-extension=.processed
app.email-from=${spring.mail.from}

# OpenAPI Configuration
springdoc.api-docs.path=/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
```

### Dockerfile

```dockerfile
FROM openjdk:21-jdk-slim
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
```

### README.md Content

Include:
- Project description and features
- Prerequisites (Java 21, Maven, SMTP access)
- Build and run instructions
- Configuration guide with Gmail App Password setup
- Input/output format examples
- API documentation links
- Docker instructions
- Troubleshooting section

**Ensure all code follows Spring Boot best practices, includes proper error handling, comprehensive logging, and clean separation of concerns. The application should be production-ready with proper configuration management and testing coverage.**