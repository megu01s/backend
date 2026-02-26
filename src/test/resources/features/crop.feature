Feature: Crop Images
  Background:
    Given I have a valid JWT token for username "existingUser"
    When I upload the file "src/test/resources/images/sample.png" to "/api/images/upload"
    Then the response status should be 201

  Scenario: Crop valid region
    When I crop the image at x 10 y 10 width 50 height 50
    Then the response status should be 200

  Scenario: Reject crop with invalid region
    When I crop the image at x -5 y 10 width 50 height 50
    Then the response status should be 400