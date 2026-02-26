package com.example.imageproc.steps;

import org.springframework.beans.factory.annotation.Autowired;

import io.cucumber.java.en.When;
import static io.restassured.RestAssured.given;
import io.restassured.response.Response;

public class ImageCropSteps {

    @Autowired
    private TestContext testContext;

    @When("I crop the image at x {int} y {int} width {int} height {int}")
    public void cropImage(int x, int y, int w, int h) {
        Long id = testContext.getResponse().jsonPath().getLong("id");

        Response response = given()
                .header("Authorization", "Bearer " + testContext.getJwtToken())
                .queryParam("x", x)
                .queryParam("y", y)
                .queryParam("width", w)
                .queryParam("height", h)
                .post("/api/images/" + id + "/crop");

        testContext.setResponse(response);
    }
}
