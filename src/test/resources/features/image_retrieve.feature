Feature: Retrieve Original Images
  Background:
    Given I have a valid JWT token for username "existingUser"
    When I upload the file "src/test/resources/images/sample.png" to "/api/images/upload"
    Then the response status should be 201

  Scenario: Retrieve an uploaded image by ID
    When I GET the uploaded image by its ID
    Then the response status should be 200

  Scenario: Reject retrieval with invalid ID
    When I GET "/api/images/999999" with the token
    Then the response status should be 404

  Scenario: Reject retrieval without JWT
    When I GET "/api/images/1" without a token
    Then the response status should be 401