Feature: Resize Image
  Background:
    Given I have a valid JWT token for username "existingUser"
    When I upload the file "src/test/resources/images/sample.png" to "/api/images/upload"
    Then the response status should be 201

  Scenario: Resize image to valid dimensions
    When I resize the image to width 100 and height 80
    Then the response status should be 200

  Scenario: Reject resize with negative width
    When I resize the image to width -10 and height 80
    Then the response status should be 400

  Scenario: Reject resize without token
    Given I clear the JWT token
    When I resize the last uploaded image to width 100 and height 80 without a token
    Then the response status should be 401