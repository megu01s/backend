Feature: Flip Images
  As a user
  I want to flip images horizontally or vertically

  Background:
    Given I have a valid JWT token for username "existingUser"
    When I upload the file "src/test/resources/images/sample.png" to "/api/images/upload"
    Then the response status should be 201

  Scenario: Flip horizontally
    When I flip the image with mode "H"
    Then the response status should be 200

  Scenario: Flip vertically
    When I flip the image with mode "V"
    Then the response status should be 200

  Scenario: Reject invalid flip mode
    When I flip the image with mode "X"
    Then the response status should be 400