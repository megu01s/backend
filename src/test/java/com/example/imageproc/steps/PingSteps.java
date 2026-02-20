package com.example.imageproc.steps;

import io.cucumber.java.en.Given;

public class PingSteps {

    @Given("a test is running")
    public void aTestIsRunning() {
        System.out.println("Cucumber is running successfully!");
    }
}
