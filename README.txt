1.	Start Solr:
	solr start

2.	Create the following two cores in Solr
	solr create_core -c task2
	solr create_core -c rural

3.	Import the Project_F17 folder into Eclipse

4.	Right-click on Project_F17 and select Update Project from the Maven menu

5.	Download Stanford Core NLP and MIT JWI and link them to the external libraries

	Stanford Core NLP:
	http://nlp.stanford.edu/software/stanford-corenlp-full-2017-06-09.zip

	MIT JWI:
	https://projects.csail.mit.edu/jwi/download.php?f=edu.mit.jwi_2.4.0.jar

6.	Go to Task2.java and run it. A file called file_rural.xml will be generated in the project directory. Post it to the core using:

	post -c task2 file_rural.xml

7.	Go to Task3.java and run it. A file called file_rural_task3_final.xml  will be generated in the project directory. Post it to the core using:

	post -c task2 file_rural_task3_final.xml

	(Note: In case it takes too much time, just go to the artNumber loop and do it in two parts.)

8.	Go to Main.java

9.	Edit String query to with your custom query

10.	Hit Run