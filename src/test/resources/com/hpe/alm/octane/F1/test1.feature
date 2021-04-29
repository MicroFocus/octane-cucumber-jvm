#Auto generated NGA revision tag
@TID2001REV0.2.0
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
        Scenario Outline: Table <id> test
            Given hello "<name>"
            When what "<question>"
            Then wow

            Examples:
                |id |name	|question	|
                | 1	|dan	|what		|
                | 2	|sari	|who		|