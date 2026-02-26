Feature: Mirror Image
  As a user
  I want to horizontally mirror an image

  Background:
    Given I have a valid JWT token for username "existingUser"
    When I upload the file "src/test/resources/images/sample.png" to "/api/images/upload"
    Then the response status should be 201

  Scenario: Successfully mirror an image
    When I mirror the image
    Then the response status should be 200