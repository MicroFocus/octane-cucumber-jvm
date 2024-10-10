Feature: test Feature
  Rule: Must have a test
    Background:
      Given back
      And back
    Scenario: test scenario
      Given test
      When test
      And test
      Then test
      But test

    Scenario: test scenario2
      Given test
      When test
      And test
      Then test
      But test

  Rule: Must not have a test
    Background:
      Given back1
      And back1
    Scenario Outline: Table <id> test
      Given hello "<name>"
      When what "<question>"
      Then wow

      Examples:
        |id |name	|question	|
        | 1	|dan	|what		|
        | 2	|sari	|who		|