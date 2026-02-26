Feature: Convert Image Format
  As a user
  I want to convert an image to different formats

  Background:
    Given I have a valid JWT token for username "existingUser"
    When I upload the file "src/test/resources/images/sample.png" to "/api/images/upload"
    Then the response status should be 201

  Scenario: Convert PNG to JPG
    When I convert the image to format "jpg"
    Then the response status should be 200

  Scenario: Convert PNG to PNG
    When I convert the image to format "png"
    Then the response status should be 200

  Scenario: Reject unsupported format
    When I convert the image to format "tiff"
    Then the response status should be 400