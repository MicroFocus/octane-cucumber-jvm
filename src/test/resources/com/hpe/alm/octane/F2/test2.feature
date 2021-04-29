#Auto generated NGA revision tag
@TID2001REV0.2.0
Feature: test Feature2
	Background:
		Given back2
		And back2
	Scenario: test scenario2
		Given test2
		When test2
		Then test2
	Scenario Outline: Table <id2>
		Given hello "<name>"
		When what "<question>"
		Then wow

		Examples:
			|id2|name	|question	|
			| 1	|dan	|what		|
			| 2	|sari	|who		|
	Scenario: test scenario3
		Given test3
		When test3
		Then test3
	Scenario Outline: Table TTT4
		Given hello "<name>"
		When what "<question>"
		Then wow

		Examples:
			|name	|question	|
			|dan	|what		|
			|sari	|who		|