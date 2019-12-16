Programming Language: Java

Steps to Execute:

	1) Open 2 terminals to execute the jar files

	2) Change directories to where the jar files exist

	3) First run the server.jar file (the number at the end can change. This is just the port number to establish a connection)

		i.e.) for OSX terminal: java -jar server.jar 471

	4) Secondly run the client.jar file (with the same port number)

		i.e.) for OSX terminal: java -jar client.jar 471

	5) Once both are opened, on the terminal running the client.jar file, you will be allowed to run the following commands

		- get  <filename>	(download files from the server)
		- put <filename>	(upload files from the client into the server)
		- ls			(list the files that exist in the server)
		- quit 			(exits the program)

	6) When using the get or put, make sure that the user includes the file name including the .txt part (i.e. GameOfThrones.txt)

	7) Complete!


Additional Notes:

	- Made a separate folder called server to show that it is passing the files through
	- The port is opening and closing every time