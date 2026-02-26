Feature: Image Filters
  As a user
  I want to apply filters to images

  Background:
    Given I have a valid JWT token for username "existingUser"
    When I upload the file "src/test/resources/images/sample.png" to "/api/images/upload"
    Then the response status should be 201

  Scenario: Apply grayscale filter
    When I apply filter "grayscale"
    Then the response status should be 200

  Scenario: Apply sepia filter
    When I apply filter "sepia"
    Then the response status should be 200

  Scenario: Reject unknown filter
    When I apply filter "unknown"
    Then the response status should be 400