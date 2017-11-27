This program includes a GUI component for extra credit.
To run the gui portion, comment out "BankingSystem.promptMainMenu();" in ProgramLauncher.java line 15
 and then uncomment "BankingSystem.gui();" in ProgramLauncher.java line 16

*****************************

INSTRUCTIONS TO RUN

The create.clp and drop.clp script will assume that the database cs157a exist
if it does not exist yet, please create it using CREATE DATABASE cs157a

On the mac, first start docker
then open terminal and type in
eval $(docker-machine env ibm-db2) && docker exec -it db2server bash -c "su - db2inst1"

type in vi create.clp and press A to insert the contents of the create.clp I have included
then press esc and press : and type in wq and enter to save

type in vi drop.clp and press A to insert the contents of the drop.clp I have included
then press esc and press : and type in wq and enter to save

to run the create file, type in db2 -tvf create.clp; into the db2 server terminal and it should create the tables and views
to run the drop file, type in db2 -tvf drop.clp; into the db2 server terminal and it should drop the tables and views

In the db.properties file please change the ip, user, password to match yours or it will not work.

cd into the folder where you stored all the java files and run "javac *.java" to compile
you will need to include db2jcc4.jar in that folder as well
Then run "java -cp ":./db2jcc4.jar" ProgramLauncher db.properties"
