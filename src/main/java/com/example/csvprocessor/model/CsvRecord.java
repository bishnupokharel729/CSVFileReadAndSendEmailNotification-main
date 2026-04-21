package com.example.csvprocessor.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CsvRecord {

    private String zipCode;
    private String email;
    private String state;
}
