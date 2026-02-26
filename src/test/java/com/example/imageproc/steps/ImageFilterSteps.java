package com.example.imageproc.steps;

import io.cucumber.java.en.When;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;

import static io.restassured.RestAssured.given;

public class ImageFilterSteps {

    @Autowired
    private TestContext testContext;

    @When("I apply filter {string}")
    public void applyFilter(String filter) {
        Long id = testContext.getResponse().jsonPath().getLong("id");

        Response response = given()
                .header("Authorization", "Bearer " + testContext.getJwtToken())
                .queryParam("type", filter)
                .post("/api/images/" + id + "/filter");

        testContext.setResponse(response);
    }
}
