Feature: Passenger Service
  Scenario: Retrieve all passengers from database
    When I request all passengers from database through service
    Then all passengers request complete with code 200 (OK)
    And first passengers page should be returned
  Scenario: Retrieve all passengers from database with male gender
    When I request all male passengers from database through service
    Then all male passengers request complete with code 200 (OK)
    And first page of filtered with gender "MALE" passengers should be returned
  Scenario: Retrieve passenger by id from database
    When I request passenger with id = 1 from database through service
    Then find by id request complete with code 200 (OK) for passenger
    And returned passenger must be with name "Andrew", email "a.sp@mail.com" and phone number "+909-987-324"
  Scenario: Retrieve non-existing passenger from database by id
    When I request passenger with id = 101 from database through service
    Then request complete with code 404(NOT_FOUND) and indicates that specified passenger not found
  Scenario: Save new passenger into database
    When I save new passenger with name "Andrew", email "a.uk@mail.com" and phone number "+111-229-101"
    Then saved passenger with name "Andrew", email "a.uk@mail.com", phone number "+111-229-101" should be returned
  Scenario: Save new passenger into database with duplicate email
    When I save new passenger with name "Dany", email "a.uk@mail.com" and phone number "+778-222-700"
    Then the response should indicate with code 400 that email already owned by another passenger
  Scenario: Update existing passenger
    When I try to update passenger with id = 1 changing name to "Daniel" and email to "dan.red@mail.com"
    Then updated passenger with name "Daniel" and email "dan.red@mail.com" should be returned
  Scenario: Update existing passenger with email already defined within database
    When I try to update passenger with id = 1 changing email to "a.sp@mail.com"
    Then the response should indicate with code 400 that updated email already owned by another passenger
  Scenario: Soft delete of passenger
    When I try to delete passenger with id = 1
    Then the response should indicate successful delete of passenger with code 204(NO_CONTENT)
  Scenario: Soft delete of passenger with non-existing id
    When I try to delete passenger with id = 500
    Then request complete with code 404(NOT_FOUND) and indicates that specified for delete passenger not found
