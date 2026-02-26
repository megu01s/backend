package com.example.imageproc.steps;

import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import io.restassured.response.Response;
import static io.restassured.RestAssured.given;

public class ImageResizeSteps {

    @Autowired
    private TestContext testContext;

    @When("I resize the image to width {int} and height {int}")
    public void resizeImage(int width, int height) {
        Long id = testContext.getResponse().jsonPath().getLong("id");

        Response response = given()
                .header("Authorization", "Bearer " + testContext.getJwtToken())
                .queryParam("width", width)
                .queryParam("height", height)
                .post("/api/images/" + id + "/resize");

        testContext.setResponse(response);
    }

    @When("I resize the last uploaded image to width {int} and height {int} without a token")
    public void resizeWithoutToken(int width, int height) {
        Long id = testContext.getResponse().jsonPath().getLong("id");

        Response response = given()
                .queryParam("width", width)
                .queryParam("height", height)
                .post("/api/images/" + id + "/resize");

        testContext.setResponse(response);
    }
}
