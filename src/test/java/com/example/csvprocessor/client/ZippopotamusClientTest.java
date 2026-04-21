package com.example.csvprocessor.client;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ZippopotamusClientTest {

    @Test
    void resolvesStateFromApi() throws Exception {
        try (MockWebServer server = new MockWebServer()) {
            server.enqueue(new MockResponse()
                    .setResponseCode(200)
                    .setBody("{\"places\":[{\"state\":\"California\"}]}"));
            server.start();

            WebClient webClient = WebClient.builder()
                    .baseUrl(server.url("/").toString())
                    .build();
            ZippopotamusClient client = new ZippopotamusClient(webClient);

            String state = client.resolveState("90210");

            assertEquals("California", state);
        }
    }

    @Test
    void returnsUnknownWhenZipCodeIsInvalid() throws Exception {
        try (MockWebServer server = new MockWebServer()) {
            server.enqueue(new MockResponse().setResponseCode(404));
            server.start();

            WebClient webClient = WebClient.builder()
                    .baseUrl(server.url("/").toString())
                    .build();
            ZippopotamusClient client = new ZippopotamusClient(webClient);

            String state = client.resolveState("00000");

            assertEquals("UNKNOWN", state);
        }
    }
}
