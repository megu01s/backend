Feature: User Authentication
  As a user of the Image Processing API
  I want to register and log into my account
  So that I can access protected image management features using JWT authentication

  # -------------------------------
  # SIGN-UP (ACCOUNT CREATION)
  # -------------------------------
  Scenario: Successfully sign up a new user
    Given a sign-up request with username "newuser" and password "StrongPass123"
    When I POST to "/api/auth/signup"
    Then the response status should be 201
    And the response should contain a message "User created successfully"

  Scenario: Reject sign-up with an existing username
    Given a sign-up request with username "existingUser" and password "Password123"
    When I POST to "/api/auth/signup"
    Then the response status should be 409
    And the response should contain a message "Username already exists"

  Scenario: Reject sign-up with invalid password format
    Given a sign-up request with username "user123" and password "short"
    When I POST to "/api/auth/signup"
    Then the response status should be 400
    And the response should contain a validation error

  # -------------------------------
  # LOGIN (JWT GENERATION)
  # -------------------------------
  Scenario: Successfully log in and receive JWT token
    Given a valid login request with username "existingUser" and password "Password123"
    When I POST to "/api/auth/login"
    Then the response status should be 200
    And the response should contain a JWT token

  Scenario: Reject login with invalid credentials
    Given a login request with username "existingUser" and password "wrongPassword"
    When I POST to "/api/auth/login"
    Then the response status should be 401
    And the response should contain an authentication error

  # -------------------------------
  # JWT-PROTECTED ROUTES
  # -------------------------------
  Scenario: Access a protected endpoint with a valid JWT token
    Given I have a valid JWT token for username "existingUser"
    When I GET "/api/images/list" with the token
    Then the response status should be 200
    And the response should contain the user's image list

  Scenario: Reject access to a protected endpoint without token
    When I GET "/api/images/list" without a token
    Then the response status should be 401
    And the response should contain an authentication error

  Scenario: Reject access to a protected endpoint with an invalid JWT token
    Given I have an invalid JWT token
    When I GET "/api/images/list" with the token
    Then the response status should be 401
    And the response should contain an authentication error
