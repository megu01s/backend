package com.example.imageproc.steps;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import com.example.imageproc.repository.UserRepository;

import io.cucumber.java.Before;
import io.restassured.RestAssured;

public class Hooks {

    @LocalServerPort
    private Integer serverPort; // IMPORTANT: must be Integer not int

    @Autowired(required = false)
    private TestRestTemplate restTemplate;

    @Autowired(required = false)
    private UserRepository userRepository;

    @Before(order = 0)
    public void setupRestAssured() {

        // Safety: if Spring context is missing, do NOT crash all tests
        if (serverPort == null || serverPort == 0) {
            System.out.println("WARNING: @LocalServerPort not injected — using fallback 8080");
            serverPort = 8080;
        }

        RestAssured.baseURI = "http://localhost";
        RestAssured.port = serverPort;

        System.out.println("RestAssured configured: " + RestAssured.baseURI + ":" + RestAssured.port);
    }

    @Before(order = 1)
    public void ensureUser() {

        if (restTemplate == null) {
            System.out.println("WARNING: TestRestTemplate not injected — skipping user creation");
            return;
        }

        try {
            String jsonBody = """
                {"username":"existingUser",
                 "email":"existingUser@example.com",
                 "password":"Password123"}
            """;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

            restTemplate.postForEntity(
                    "http://localhost:" + serverPort + "/api/auth/signup",
                    entity,
                    String.class
            );

        } catch (Exception ignored) {
            // Ignore 409 or connectivity errors
        }
    }
}
