package com.example.csvprocessor.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ZippopotamusResponse {

    @JsonProperty("places")
    private List<Place> places;

    public String getState() {
        if (places == null || places.isEmpty()) {
            return null;
        }
        return places.get(0).getState();
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Place {
        @JsonProperty("state")
        private String state;
    }
}
