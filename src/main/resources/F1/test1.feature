#Auto generated NGA revision tag
@TID2001REV0.2.0
Feature: test Feature
	Background:
		Given back
		And back
	Scenario: test scenario
		Given test
		When test
		Then test
	Scenario Outline: Table TTT
		Given hello "<name>"
		When what "<question>"
		Then wow

		Examples:
			|name	|question	|
			|dan	|what		|
			|sari	|who		|