Author: Brian Wu
Date: October 7th

Currently only Part 1 of the project is fully working
To compile, run "bash compile.sh" to compile all the files
Then to see a demonstration of the program, run
"java DriverPart1 <listenPort> 0" on one terminal.
"java DriverPart1 <listenPort + 1> 1" on terminal 2.
"java DriverPart1 <listenPort + 2> 2" on terminal 2.
"java DriverPart1 <listenPort + 3> 3" on terminal 2.

The program is designed to be able to run on as may nodes as needed, but needs
to be implemented.

The program should be configurable to run on dcXX.utdallas.edu machines
and run as many nodes as needed, but I couldn't implement it on time including
the other parts of the project.
The MAP protocol works and outputfiles should be generated featuring the
application messages sent from each node in the form of "NodeXAppMessages.txt"
The content of the messages is a randomized number from 0-99.
A config file is needed to set the 6 parameters, and a sample one is already included.
Part 2,3 of the project would be able to implemented if given more time. The skeleton of the program is there, but needs to be implemented.


