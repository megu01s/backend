package com.example.imageproc.config;

import io.cucumber.spring.CucumberContextConfiguration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, properties = "spring.main.allow-bean-definition-overriding=true")
@Import(TestSecurityConfig.class)
public class CucumberSpringConfig {
}
