package com.example.imageproc.steps;

import static io.restassured.RestAssured.*;

import org.springframework.beans.factory.annotation.Autowired;

import io.cucumber.java.en.When;
import io.restassured.response.Response;

public class ImageFlipSteps {

    @Autowired
    private TestContext testContext;

    @When("I flip the image with mode {string}")
    public void flipImage(String mode) {
        Long id = testContext.getResponse().jsonPath().getLong("id");

        Response response = given()
                .header("Authorization", "Bearer " + testContext.getJwtToken())
                .queryParam("direction", mode)
                .post("/api/images/" + id + "/flip");

        testContext.setResponse(response);
    }
}
