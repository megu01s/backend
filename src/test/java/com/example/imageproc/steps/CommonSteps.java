package com.example.imageproc.steps;

import org.springframework.beans.factory.annotation.Autowired;

import io.cucumber.java.en.Then;
import io.restassured.response.Response;

public class CommonSteps {

    @Autowired
    private TestContext testContext;

    @Then("the response status should be {int}")
    public void theResponseStatusShouldBe(int status) {
        Response response = testContext.getResponse();
        response.then().statusCode(status);
    }
}
