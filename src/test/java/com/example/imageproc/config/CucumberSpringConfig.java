package com.example.imageproc.config;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import io.cucumber.spring.CucumberContextConfiguration;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, properties = "spring.main.allow-bean-definition-overriding=true")
@Import(TestSecurityConfig.class)
@ActiveProfiles("test") // <-- active le profil "test" pour les tests Cucumber
public class CucumberSpringConfig {
}
