package com.example.imageproc.steps;

import org.springframework.beans.factory.annotation.Autowired;

import io.cucumber.java.en.When;
import static io.restassured.RestAssured.given;
import io.restassured.response.Response;

public class ImageRotateSteps {

    @Autowired
    private TestContext testContext;

    @When("I rotate the image by {int} degrees")
    public void rotateImage(int deg) {
        // On récupère l'ID de la dernière image uploadée depuis la réponse Cucumber
        Long id = testContext.getResponse().jsonPath().getLong("id");

        // On fait la requête vers l’endpoint de rotation
        Response response = given()
                .header("Authorization", "Bearer " + testContext.getJwtToken())
                .queryParam("degrees", deg)
                .post("/api/images/" + id + "/rotate");

        // On stocke la réponse pour le step suivant
        testContext.setResponse(response);
    }
}
