@ATag
Feature: asd

  Background:
    Given I am logged in
    And I'm failed

  Scenario: scenario with passed steps
    When passed step
    Then another passed step

  Scenario: scenario with failed steps
    When failed step
    Then another failed step

  Scenario: scenario with skipped steps
    When pending step
    Then another pending step