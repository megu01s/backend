Feature: Watermark Text
  As a user
  I want to apply a text watermark to my image

  Background:
    Given I have a valid JWT token for username "existingUser"
    When I upload the file "src/test/resources/images/sample.png" to "/api/images/upload"
    Then the response status should be 201

  Scenario: Apply watermark text
    When I watermark the image with text "Hello World"
    Then the response status should be 200

  Scenario: Reject empty watermark text
    When I watermark the image with text ""
    Then the response status should be 400

  Scenario: Reject too long watermark text
    When I watermark the image with text "Lorem ipsum dolor sit amet, consectetur adipiscing elit."
    Then the response status should be 400