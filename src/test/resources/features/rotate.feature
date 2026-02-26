Feature: Rotate Images
  Background:
    Given I have a valid JWT token for username "existingUser"
    When I upload the file "src/test/resources/images/sample.png" to "/api/images/upload"
    Then the response status should be 201

  Scenario: Rotate 90 degrees
    When I rotate the image by 90 degrees
    Then the response status should be 200

  Scenario: Reject rotate invalid degrees
    When I rotate the image by 999 degrees
    Then the response status should be 400