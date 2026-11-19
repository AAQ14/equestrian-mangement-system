Feature: Booking a riding session
  As a client
  I want to book a riding session
  So that I can reserve my spot with an instructor

  Background:
    Given an active client with id 100
    And an available riding session tomorrow with capacity 2

  Scenario: Client books an available session
    When the client books the session
    Then the booking is created with status "CONFIRMED"
    And a booking event is published to Kafka

  Scenario: Client cannot book the same session twice
    Given the client has already booked the session
    When the client books the session
    Then the booking is rejected with message containing "already has a booking"

  Scenario: Last spot makes the session full
    Given the session capacity is 1
    When the client books the session
    Then the session status is "FULL"

  Scenario: Client cannot book a cancelled session
    Given the session is cancelled
    When the client books the session
    Then the booking is rejected with message containing "not available"