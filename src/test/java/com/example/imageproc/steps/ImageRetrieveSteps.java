package com.example.imageproc.steps;

import org.springframework.beans.factory.annotation.Autowired;

import io.cucumber.java.en.When;
import static io.restassured.RestAssured.given;
import io.restassured.response.Response;

public class ImageRetrieveSteps {

    @Autowired
    private TestContext testContext;

    private Long lastUploadedId;

    @When("I GET the uploaded image by its ID")
    public void getUploadedImage() {
        Long id = testContext.getResponse().jsonPath().getLong("id");

        Response response = given()
                .header("Authorization", "Bearer " + testContext.getJwtToken())
                .get("/api/images/" + id);

        testContext.setResponse(response);
    }
}
