# Agent-Based Modelling of Traffic Evacuation Scenarios
### Simulation software for evaluating and optimising evacuation strategy algorithms
##### A Masters of Computer Science dissertation project 

README Contents:
	- Structure of project Directory
	- Running the programs
	- Compiling the programs

----------------------------------------------------------------------------------

#### Structure of the project directory:

EvacVisual.jar				-	The way to run the visualisation of a simulation

ExperimentSequence.jar 		-	The way to run simulations in the background with data recording

config/						-	All simulation configuration (.xml) files, and network (.net) files. The author has created a range of example simulation configuration files for readers to try out, and use as references when making your own .xml config files

config/experiments_by_author/	- The (mostly failed) experiments previously ran

config/networks/				- A series of .net files which can be loaded by the software	


lib/						-	All project dependencies, used to compile the project

results/					-	All results taken from experiments found under config/experiments_by_author

src/						-	All source code

README.txt					-	This file

-----------------------------------------------------------------------------------

#### Running the programs:

This project is ran from the command line.

1) From the terminal, navigate to the root project directory (which contains EvacVisual.jar and ExperimentSequence.jar)

2.a) For visualisation, execute the command:

	java -jar EvacVisual.jar <PATH TO CONFIGURATION FILE>

By default, the path to configuration file begins at the root project directory.
I.e - the following is a valid command:
	
	java -jar EvacVisual.jar config/example1.xml

Note that EvacVisual will not iterate through a series of independent variable values. If you visualise a config
file which has independent variables it will only visualise the first configuration (where the independent variables
are at their minimums)

2.b) For experiments:
	
	java -jar ExperimentSequence.jar -o <Output Location> -i <Input config xml file(s)>

Here, the results .txt files will be placed in the folder specified by <Output Location>
<Input config xml file(s)> can take the following forms:
	
A regular path to a configuration file:
	
	config/example1.xml

A list of paths to config files, simply space-separated:
		
	config/example1.xml config/example2.xml
		
A directory which contains config files:
	
	config/exampleBatch/

The following is a valid command:
	
	java -jar ExperimentSequence.jar -o output/ -i config/exampleBatch/
