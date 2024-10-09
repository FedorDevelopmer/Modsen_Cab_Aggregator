Feature: Driver Service
  Scenario: Retrieve all drivers from database
    When I request all drivers from database through service
    Then all drivers request complete with code 200 (OK)
    And first drivers page should be returned
  Scenario: Retrieve all drivers from database with male gender
    When I request all male drivers from database through service
    Then all male drivers request complete with code 200 (OK)
    And first page of filtered with gender "MALE" drivers should be returned
  Scenario: Retrieve driver by id from database
    When I request driver by id = 1 from database through service
    Then find by id request complete with code 200 (OK) for driver
    And returned driver must not be null, with name "Jack", email "j.sp@mail.com" and phone number "+243-424-98"
  Scenario: Retrieve non-existing driver from database by id
    When I request driver by id = 101 from database through service
    Then request complete with code 404(NOT_FOUND) and indicates that specified driver not found
  Scenario: Save new driver into database
    When I save new driver with name "Larry", email "l.yo@mail.com" and phone number "+777-909-101"
    Then saved driver with name "Larry", email "l.yo@mail.com", phone number "+777-909-101" and defined id should be returned
  Scenario: Save new driver into database with duplicate email
    When I save new driver with name "Jeremy", email "l.yo@mail.com" and phone number "+988-222-000"
    Then the response should indicate that email already owned by another driver with code 400
  Scenario: Update existing driver
    When I try to update driver with id = 1 changing name to "Daniel" and email to "dan.red@gmail.com"
    Then updated driver with name "Daniel" and email "dan.red@gmail.com" should be returned
  Scenario: Update existing driver with email already defined within database
    When I try to update driver with id = 1 changing email to "l.yo@mail.com"
    Then the response should indicate that updated email already owned by another driver with code 400
  Scenario: Soft Delete Of Driver
    When I try to delete driver with id = 1
    Then the response should indicate successful delete of driver with code 204(NO_CONTENT)
  Scenario: Soft Delete Of Driver with non-existing id
    When I try to delete driver with id = 2002
    Then request complete with code 404(NOT_FOUND) and indicates that specified for delete driver not found
