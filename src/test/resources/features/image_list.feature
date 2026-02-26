Feature: Image Listing
  As an authenticated user
  I want to list my uploaded images
  So that I can see all stored image metadata

  Background:
    Given I have a valid JWT token for username "existingUser"

  # -----------------------------------------------------
  # SUCCESS CASE
  # -----------------------------------------------------
  Scenario: List all uploaded images
    When I GET "/api/images" with the token
    Then the response status should be 200
    And the response should contain the user's image list

  # -----------------------------------------------------
  # AUTH ERRORS
  # -----------------------------------------------------
  Scenario: Reject listing without authentication token
    When I GET "/api/images" without a token
    Then the response status should be 401
    And the response should contain an authentication error

  Scenario: Reject listing with invalid authentication token
    Given I have an invalid JWT token
    When I GET "/api/images" with the token
    Then the response status should be 401
    And the response should contain an authentication error