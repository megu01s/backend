package com.example.imageproc.steps;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import io.cucumber.java.Before;
import io.restassured.RestAssured;
import static io.restassured.RestAssured.baseURI;

public class Hooks {

    @LocalServerPort
    int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Before(order = 0)
    public void setupRestAssured() {
        if (port == 0) {
            // Fallback to default port if not set
            port = 8080;
        }
        baseURI = "http://localhost";
        RestAssured.port = port;
    }

    @Before(order = 1)
    public void setupTestUser() {
        // Create the "existingUser" for tests that need it
        try {
            String jsonBody = "{\"username\": \"existingUser\", \"email\": \"existingUser@example.com\", \"password\": \"Password123\"}";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

            restTemplate.postForEntity(
                    "http://localhost:" + port + "/api/auth/signup",
                    entity,
                    String.class
            );
        } catch (Exception e) {
            // User might already exist, ignore
        }
    }
}
