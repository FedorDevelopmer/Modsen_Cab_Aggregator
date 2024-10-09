Feature: Ride Service
  Scenario: Retrieve all rides scores from database
    When I request all rides from database through service
    Then all rides request complete with code 200 (OK)
    And first rides page should be returned
  Scenario: Retrieve all rides from database with defined passenger id
    When I request all rides with passenger id = 1 from database through service
    Then all rides filtered by passenger id request complete with code 200 (OK)
    And first page of filtered by passenger id = 1 rides should be returned
  Scenario: Retrieve ride by id from database
    When I request ride with id = 1 from database through service
    Then find by id request complete with code 200 (OK) of rides
    And returned ride must be with price = 12.00 and status "CREATED"
  Scenario: Retrieve non-existing ride from database by id
    When I request ride with id = 205 from database through service
    Then request complete with code 404(NOT_FOUND) and indicates that specified ride not found
  Scenario: Save new ride into database
    When I save new ride with driver id = 1, passenger id = 1 and price = 15.00
    Then saved ride with driver id = 1, passenger id = 1 and price = 15.00 should be returned
  Scenario: Update existing ride
    When I try to update ride with id = 1 changing price to 25.00
    Then updated ride with price = 25.00 should be returned
  Scenario: Change ride status
    When I try to change status of ride with id = 1 to "CANCELLED"
    Then ride with changed status "CANCELLED" should be returned
  Scenario: Delete of ride
    When I try to delete ride with id = 1
    Then the response should indicate successful delete of ride with code 204(NO_CONTENT)
  Scenario: Delete of ride with non-existing id
    When I try to delete ride with id = 999
    Then request complete with code 404(NOT_FOUND) and indicates that specified for delete ride not found
