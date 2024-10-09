Feature: Car Service
  Scenario: Retrieve all cars from database
    When I request all cars from database through service
    Then all cars request complete with code 200 (OK)
    And first cars page should be returned
  Scenario: Retrieve all cars from database with brand "Honda"
    When I request all cars with brand "Honda" from database through service
    Then all "Honda" cars request complete with code 200 (OK)
    And first page of filtered with brand "Honda" cars should be returned
  Scenario: Retrieve car by id from database
    When I request car by id = 1 from database through service
    Then find by id request complete with code 200 (OK) for car
    And returned car must not be null, with brand "Renault" and registration number "7 TAX 5469"
  Scenario: Retrieve non-existing car from database by id
    When I request car by id = 101 from database through service
    Then request complete with code 404(NOT_FOUND) and indicates that specified car not found
  Scenario: Save new car into database
    When I save new car with brand "Wolkswagen" and registration number "7 TAX 4543" for driver with id 1
    Then saved car with brand "Wolkswagen", registration number "7 TAX 4543" and driver id 1 should be returned
  Scenario: Save new driver into database with duplicate registration number
    When I save new car with brand "Skoda" and registration number "7 TAX 4543" for driver with id 1
    Then the response should indicate that registration number already owned by another car with code 400
  Scenario: Update existing car
    When I try to update car with id = 1 changing brand to "Audi" and registration number to "7 TAX 8989"
    Then updated driver with brand "Audi" and registration number to "7 TAX 8989" should be returned
  Scenario: Update existing car with registration number already defined within database
    When  I try to update car with id = 2 changing registration number to "7 TAX 4543"
    Then the response should indicate that updated registration number already owned by another driver with code 400
  Scenario: Soft delete of car
    When I try to delete car with id = 1
    Then the response should indicate successful delete of car with code 204(NO_CONTENT)
  Scenario: Soft delete of car with non-existing id
    When I try to delete car with id = 205
    Then request complete with code 404(NOT_FOUND) and indicates that specified for delete car not found
