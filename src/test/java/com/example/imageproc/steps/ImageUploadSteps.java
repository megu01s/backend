package com.example.imageproc.steps;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.springframework.beans.factory.annotation.Autowired;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;

public class ImageUploadSteps {

    @Autowired
    private TestContext testContext;

    @When("I upload the file {string} to {string}")
    public void uploadFile(String filePath, String endpoint) throws IOException {
        String jwt = testContext.getJwtToken();
        File file = new File(filePath);
        byte[] fileContent = Files.readAllBytes(file.toPath());

        Response response = given()
                .header("Authorization", "Bearer " + jwt)
                .multiPart("file", file.getName(), fileContent, "image/png")
                .when()
                .post(endpoint);

        testContext.setResponse(response);
    }

    @When("I upload the file {string} to {string} without a token")
    public void uploadFileWithoutToken(String path, String endpoint) {
        Response response = given()
                .multiPart("file", new File(path))
                .when()
                .post(endpoint);

        testContext.setResponse(response);
    }

    @Given("I clear the JWT token")
    public void clearJwt() {
        testContext.clearJwt();
    }

    @Then("the response should contain the image metadata")
    public void responseContainsImageMetadata() {
        testContext.getResponse().then().body("id", notNullValue());
        testContext.getResponse().then().body("fileName", notNullValue());
        testContext.getResponse().then().body("fileType", notNullValue());
    }

}
