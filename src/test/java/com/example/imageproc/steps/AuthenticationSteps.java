package com.example.imageproc.steps;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

import org.springframework.beans.factory.annotation.Autowired;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;

public class AuthenticationSteps {

    @Autowired
    private TestContext testContext;

    private String username;
    private String password;

    // SIGN-UP
    @Given("a sign-up request with username {string} and password {string}")
    public void aSignUpRequest(String user, String pwd) {
        this.username = user;
        this.password = pwd;
    }

    @Then("the response should contain a message {string}")
    public void responseShouldContainMessage(String message) {
        testContext.getResponse().then().body("message", equalTo(message));
    }

    @Then("the response should contain a validation error")
    public void responseShouldContainValidationError() {
        testContext.getResponse().then().body("errors", notNullValue());
    }

    // LOGIN
    @Given("a valid login request with username {string} and password {string}")
    public void validLoginRequest(String user, String pwd) {
        this.username = user;
        this.password = pwd;
    }

    @Given("a login request with username {string} and password {string}")
    public void aLoginRequest(String user, String pwd) {
        this.username = user;
        this.password = pwd;
    }

    @Then("the response should contain a JWT token")
    public void responseShouldContainJwtToken() {
        Response response = testContext.getResponse();
        String jwt = response.jsonPath().getString("token");
        response.then().body("token", notNullValue());

        testContext.setJwtToken(jwt); // <-- on met le token dans le context
    }

    @Given("I have a valid JWT token for username {string}")
    public void iHaveAValidJwtToken(String user) {
        // Try sign-up first (201 or 409 both ok), then login
        String signupBody = """
        {"username":"%s","email":"%s@example.com","password":"Password123"}
        """.formatted(user, user);

        // Sign-up (ignore 409 if already exists)
        io.restassured.response.Response signup = given()
                .contentType("application/json")
                .body(signupBody)
                .post("/api/auth/signup");

        // Now login to get the token
        String loginBody = """
        {"username":"%s","password":"Password123"}
        """.formatted(user);

        io.restassured.response.Response login = given()
                .contentType("application/json")
                .body(loginBody)
                .post("/api/auth/login");

        // Expect 200 and a token
        login.then().statusCode(200);
        String jwt = login.jsonPath().getString("token");
        if (jwt == null || jwt.isBlank()) {
            throw new IllegalStateException("JWT was null/blank after login. Response: " + login.asString());
        }
        testContext.setJwtToken(jwt);
    }

    @Given("I have an invalid JWT token")
    public void iHaveAnInvalidJwtToken() {
        testContext.setJwtToken("invalid.token.value");
    }

    // PROTECTED ENDPOINTS
    @When("I GET {string} with the token")
    public void iGetWithToken(String endpoint) {
        if (endpoint.equals("/api/images/list")) {
            endpoint = "/api/images";
        }

        String jwt = testContext.getJwtToken();

        Response response = given()
                .header("Authorization", "Bearer " + jwt)
                .get(endpoint);

        testContext.setResponse(response);
    }

    @When("I GET {string} without a token")
    public void iGetWithoutToken(String endpoint) {
        if (endpoint.equals("/api/images/list")) {
            endpoint = "/api/images";
        }
        Response response = given().get(endpoint);
        testContext.setResponse(response);
    }

    @Then("the response should contain the user's image list")
    public void responseShouldContainImageList() {
        testContext.getResponse().then().body("$", notNullValue());
    }

    @Then("the response should contain an authentication error")
    public void responseShouldContainAuthError() {
        testContext.getResponse().then().body(
                anyOf(
                        containsString("error"),
                        containsString("message"),
                        containsString("timestamp")
                )
        );
    }

    @When("I POST JSON to {string}")
    public void iPostJson(String endpoint) {
        String body = """
        {
        "username": "%s",
        "password": "%s",
        "email": "%s@example.com"
        }
        """.formatted(username, password, username);

        Response response = given()
                .contentType("application/json")
                .body(body)
                .post(endpoint);

        testContext.setResponse(response);
    }

    @When("I POST JSON with empty body to {string}")
    public void postJsonEmptyBody(String endpoint) {

        Response response = given()
                .contentType("application/json")
                // no body call:
                .body("") // EMPTY BODY
                .post(endpoint);

        testContext.setResponse(response);
    }

}
