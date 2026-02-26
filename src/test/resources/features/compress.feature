Feature: Compress Image
  As a user
  I want to compress an image to reduce file size

  Background:
    Given I have a valid JWT token for username "existingUser"
    When I upload the file "src/test/resources/images/sample.png" to "/api/images/upload"
    Then the response status should be 201

  Scenario: Compress image with valid quality
    When I compress the image with quality 0.5
    Then the response status should be 200

  Scenario: Reject invalid compress quality
    When I compress the image with quality -1
    Then the response status should be 400

  Scenario: Reject compress quality above 1
    When I compress the image with quality 2
    Then the response status should be 400