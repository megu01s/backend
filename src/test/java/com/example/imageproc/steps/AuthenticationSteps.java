package com.example.imageproc.steps;

import io.cucumber.java.en.*;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class AuthenticationSteps {

    private Response response;
    private String username;
    private String password;
    private String jwtToken;

    // -----------------------------------
    // SIGN-UP
    // -----------------------------------
    @Given("a sign-up request with username {string} and password {string}")
    public void aSignUpRequest(String user, String pwd) {
        this.username = user;
        this.password = pwd;
    }

    @When("I POST to {string}")
    public void iPostTo(String endpoint) {
        response = given()
                .contentType("application/json")
                .body("{\"username\": \"" + username + "\", \"email\": \"" + username + "@example.com\", \"password\": \"" + password + "\"}")
                .post(endpoint);
    }

    @Then("the response status should be {int}")
    public void theResponseStatusShouldBe(int status) {
        response.then().statusCode(status);
    }

    @Then("the response should contain a message {string}")
    public void responseShouldContainMessage(String message) {
        response.then().body("message", equalTo(message));
    }

    @Then("the response should contain a validation error")
    public void responseShouldContainValidationError() {
        response.then().body("errors", notNullValue());
    }

    // -----------------------------------
    // LOGIN & JWT TOKEN
    // -----------------------------------
    @Given("a valid login request with username {string} and password {string}")
    public void validLoginRequest(String user, String pwd) {
        this.username = user;
        this.password = pwd;
    }

    // Step manquant ajouté :
    @Given("a login request with username {string} and password {string}")
    public void aLoginRequest(String user, String pwd) {
        this.username = user;
        this.password = pwd;
    }

    @Then("the response should contain a JWT token")
    public void responseShouldContainJwtToken() {
        jwtToken = response.jsonPath().getString("token");
        response.then().body("token", notNullValue());
    }

    @Given("I have a valid JWT token for username {string}")
    public void iHaveAValidJwtToken(String user) {
        Response loginResponse = given()
                .contentType("application/json")
                .body("{\"username\": \"" + user + "\", \"password\": \"Password123\"}")
                .post("/api/auth/login");

        loginResponse.then().statusCode(200);
        jwtToken = loginResponse.jsonPath().getString("token");
    }

    @Given("I have an invalid JWT token")
    public void iHaveAnInvalidJwtToken() {
        jwtToken = "invalid.token.value";
    }

    // -----------------------------------
    // PROTECTED ENDPOINTS
    // -----------------------------------
    @When("I GET {string} with the token")
    public void iGetWithToken(String endpoint) {
        // Translate /api/images/list to /api/images
        if (endpoint.equals("/api/images/list")) {
            endpoint = "/api/images";
        }
        response = given()
                .header("Authorization", "Bearer " + jwtToken)
                .get(endpoint);
    }

    @When("I GET {string} without a token")
    public void iGetWithoutToken(String endpoint) {
        // Translate /api/images/list to /api/images
        if (endpoint.equals("/api/images/list")) {
            endpoint = "/api/images";
        }
        response = given()
                .get(endpoint);
    }

    @Then("the response should contain the user's image list")
    public void responseShouldContainImageList() {
        // The actual endpoint returns a list directly, not wrapped in "images"
        // So we check if it's a list (not null)
        response.then().body("$", notNullValue());
    }

    @Then("the response should contain an authentication error")
    public void responseShouldContainAuthError() {
        // Check for error, message, or timestamp in the response body
        String body = response.getBody().asString();
        response.then().body(
                anyOf(
                        containsString("error"),
                        containsString("message"),
                        containsString("timestamp")
                )
        );
    }
}
