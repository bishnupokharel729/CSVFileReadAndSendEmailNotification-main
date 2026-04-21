# CSV File Read and Send Email

A Spring Boot application that monitors an input directory for CSV files containing ZIP codes and email addresses. For each row, it resolves the state with the Zippopotam.us API, writes output to an output directory, and sends an email notification.

## Features

- Monitors `input` folder every 2 minutes
- Processes only `.csv` files
- Resolves state from `http://api.zippopotam.us/us/{zip}`
- Writes result CSV to `output` folder with timestamped filenames (`inputfilename-output-YYYYMMDDHHMMSS.csv`)
- Renames processed files with `.processed`
- Sends email notifications using Spring Boot Mail
- Uses `WebClient`, Lombok, SLF4J logging, and clean service layers
- Includes unit tests for the API client and CSV processing service
- Adds Swagger/OpenAPI UI support

## Getting Started

### Prerequisites

- Java 21
- Maven 3.9+
- Access to an SMTP server

### Build

```bash
mvn clean package
```

### Configuration

Edit `src/main/resources/application.properties` and update the mail settings:

```properties
# Gmail SMTP Configuration (recommended)
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-gmail@gmail.com
spring.mail.password=YOUR_GMAIL_APP_PASSWORD
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true
spring.mail.from=your-gmail@gmail.com

# Alternative SMTP providers (uncomment and modify as needed)
# spring.mail.host=smtp.example.com
# spring.mail.port=587
# spring.mail.username=your-email@example.com
# spring.mail.password=your-password
# spring.mail.from=no-reply@example.com

app.input-dir=./input
app.output-dir=./output
app.api.base-url=http://api.zippopotam.us/us
app.scheduler.fixed-rate-ms=120000
app.processed-extension=.processed
app.email-from=${spring.mail.from}
```

#### Gmail Setup

For Gmail accounts, you must use an **App Password** instead of your regular password:

1. Enable 2-Step Verification on your Google account
2. Go to [Google App Passwords](https://myaccount.google.com/apppasswords)
3. Select "Mail" and "Windows Computer"
4. Copy the 16-character password (without spaces) to `spring.mail.password`

### Run

```bash
mvn spring-boot:run
```

### Main steps

1. Place your CSV file in the `input` directory.
2. The app scans the input folder immediately after startup and then every 2 minutes.
3. For each record, it resolves the ZIP code state and writes `zip_code,email,state` to a new file in the `output` directory.
4. After processing, the source file is renamed to `<filename>.processed` in the `input` folder.
5. The app sends an email notification for each row using the configured SMTP settings.

### Input CSV format

Create CSV files in the `input` directory with headers:

```csv
zip_code,email
90210,test@example.com
10001,user@example.com
```

### Output

After processing, the app writes an output CSV file to the `output` directory with a timestamped filename in the format `inputfilename-output-YYYYMMDDHHMMSS.csv` (e.g., `sample-input-output-20260420143055.csv`). The input file is renamed to `*.processed` in the `input` folder.

### Swagger / OpenAPI

Once running, view API documentation at:

- `http://localhost:8080/swagger-ui.html`
- `http://localhost:8080/api-docs`

### Troubleshooting

**Email Authentication Failed**
- For Gmail: Ensure you're using an App Password, not your regular password
- Verify 2-Step Verification is enabled on your Google account
- Check that the App Password is entered without spaces

**Mail Server Connection Failed**
- Verify SMTP host and port are correct for your email provider
- Ensure firewall/antivirus isn't blocking the connection
- Check internet connectivity

**CSV Processing Errors**
- Ensure CSV files have `zip_code,email` headers
- Verify ZIP codes are valid US postal codes
- Check file permissions on input/output directories

### Docker

Build the jar first, then build the image:

```bash
mvn clean package
docker build -t csv-file-processor .
```

### Notes

- Failed ZIP lookups fallback to `UNKNOWN`
- Processing continues even if individual rows fail
- Only files ending in `.csv` are processed

## Sample CSV

See `sample-input.csv` for an example input file.
