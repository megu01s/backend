Feature: Image Upload
  As an authenticated user
  I want to upload images
  So that I can store and transform them later

  Background:
    Given I have a valid JWT token for username "existingUser"

  Scenario: Upload a valid PNG image
    When I upload the file "src/test/resources/images/sample.png" to "/api/images/upload"
    Then the response status should be 201
    And the response should contain the image metadata

  Scenario: Reject upload without token
    Given I clear the JWT token
    When I upload the file "src/test/resources/images/sample.png" to "/api/images/upload" without a token
    Then the response status should be 401
    And the response should contain an authentication error

  Scenario: Reject invalid file type
    When I upload the file "src/test/resources/images/invalid.txt" to "/api/images/upload"
    Then the response status should be 400

    # JPG valide
Scenario: Upload a valid JPG image
  When I upload the file "src/test/resources/images/sample.jpg" to "/api/images/upload"
  Then the response status should be 201
  And the response should contain the image metadata

# Token invalide
Scenario: Reject upload with invalid token
  Given I clear the JWT token
  Given I have an invalid JWT token
  When I upload the file "src/test/resources/images/sample.png" to "/api/images/upload"
  Then the response status should be 401
  And the response should contain an authentication error

# Fichier corrompu (MIME image mais non lisible)
Scenario: Reject corrupted image bytes
  When I upload the file "src/test/resources/images/corrupted.png" to "/api/images/upload"
  Then the response status should be 400

# Sans paramètre "file"
Scenario: Reject upload without form field name file
  Given I have a valid JWT token for username "existingUser"
  When I POST to "/api/images/upload" using the token but without file data
  Then the response status should be 400