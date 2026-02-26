package com.example.imageproc.steps;

import static io.restassured.RestAssured.*;

import org.springframework.beans.factory.annotation.Autowired;

import io.cucumber.java.en.When;
import io.restassured.response.Response;

public class ImageFormatSteps {

    @Autowired
    private TestContext testContext;

    @When("I convert the image to format {string}")
    public void convertImageFormat(String format) {
        Long id = testContext.getResponse().jsonPath().getLong("id");

        Response response = given()
                .header("Authorization", "Bearer " + testContext.getJwtToken())
                .queryParam("format", format)
                .post("/api/images/" + id + "/format");

        testContext.setResponse(response);
    }
}
