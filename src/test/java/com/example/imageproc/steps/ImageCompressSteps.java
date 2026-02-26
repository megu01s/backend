package com.example.imageproc.steps;

import io.cucumber.java.en.When;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;

import static io.restassured.RestAssured.given;

public class ImageCompressSteps {

    @Autowired
    private TestContext testContext;

    @When("I compress the image with quality {double}")
    public void compressImage(double quality) {
        Long id = testContext.getResponse().jsonPath().getLong("id");

        Response response = given()
                .header("Authorization", "Bearer " + testContext.getJwtToken())
                .queryParam("quality", quality)
                .post("/api/images/" + id + "/compress");

        testContext.setResponse(response);
    }
}
