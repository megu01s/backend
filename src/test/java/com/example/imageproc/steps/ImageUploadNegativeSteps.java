package com.example.imageproc.steps;

import static io.restassured.RestAssured.*;

import org.springframework.beans.factory.annotation.Autowired;

import io.cucumber.java.en.When;
import io.restassured.response.Response;

public class ImageUploadNegativeSteps {

    @Autowired
    private TestContext testContext;

    @When("I POST to {string} using the token but without file data")
    public void postWithoutFileData(String endpoint) {
        String token = testContext.getJwtToken();
        if (token == null || token.isBlank()) {
            throw new IllegalStateException("JWT is missing in TestContext; background Given must create one.");
        }

        // Send a multipart/form-data request with the "file" field but with empty content.
        // This ensures the request passes through Spring Security (validates JWT)
        // and reaches the controller, which will return 400 for empty file.
        Response response = given()
                .header("Authorization", "Bearer " + token)
                .multiPart("file", "empty.txt", new byte[0], "text/plain")
                .post(endpoint);

        testContext.setResponse(response);
    }
}
