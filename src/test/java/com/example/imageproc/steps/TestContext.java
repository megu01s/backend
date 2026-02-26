package com.example.imageproc.steps;

import org.springframework.stereotype.Component;

import io.restassured.response.Response;

@Component
public class TestContext {

    private Response response;
    private String jwtToken;

    public void setResponse(Response response) {
        this.response = response;
    }

    public Response getResponse() {
        return response;
    }

    public void setJwtToken(String jwtToken) {
        this.jwtToken = jwtToken;
    }

    public String getJwtToken() {
        return jwtToken;
    }

    public void clearJwt() {
        this.jwtToken = null;
    }
}
