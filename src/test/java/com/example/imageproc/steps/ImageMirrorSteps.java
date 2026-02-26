package com.example.imageproc.steps;

import static io.restassured.RestAssured.*;

import org.springframework.beans.factory.annotation.Autowired;

import io.cucumber.java.en.When;
import io.restassured.response.Response;

public class ImageMirrorSteps {

    @Autowired
    private TestContext testContext;

    @When("I mirror the image")
    public void mirrorImage() {
        Long id = testContext.getResponse().jsonPath().getLong("id");

        Response response = given()
                .header("Authorization", "Bearer " + testContext.getJwtToken())
                .post("/api/images/" + id + "/mirror");

        testContext.setResponse(response);
    }
}
