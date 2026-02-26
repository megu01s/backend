package com.example.imageproc.steps;

import static io.restassured.RestAssured.*;

import org.springframework.beans.factory.annotation.Autowired;

import io.cucumber.java.en.When;
import io.restassured.response.Response;

public class ImageWatermarkSteps {

    @Autowired
    private TestContext testContext;

    @When("I watermark the image with text {string}")
    public void watermarkImage(String text) {
        Long id = testContext.getResponse().jsonPath().getLong("id");

        Response response = given()
                .header("Authorization", "Bearer " + testContext.getJwtToken())
                .queryParam("text", text)
                .post("/api/images/" + id + "/watermark/text");

        testContext.setResponse(response);
    }
}
