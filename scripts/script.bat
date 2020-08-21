@echo off

GOTO START

:USAGE
    echo NOTE: Please make sure that you're running this script under the homeworks/ directory!
    echo    * compile: compiles the java code and javadocs
    echo    * run: runs the program (assumes that you've compiled first)
    echo    * server: compiles and runs the server code
    echo    * help: lists all the possible commands (brings up this list)
    echo    * clean: cleans the directories

    exit /B 1
    
:START
if [%1]==[] (
   echo Please specify an argument!
   GOTO USAGE
)
if "%1"=="help" (
   GOTO USAGE
)

::compiles program and javadocs
if "%1"=="compile" (
   echo Compiling program...
   javac -cp "lib/sqlite-jdbc-3.30.1.jar;." src\main\*.java
   echo Compiling javadocs...
   javadoc -cp "lib/sqlite-jdbc-3.30.1.jar;." -author -version -d docs src\main\*.java src\server\*.java 
   exit /B 0
)

::runs the program
if "%1"=="run" (
   echo Running program...
   java -cp "lib/sqlite-jdbc-3.30.1.jar;." src/main/Tetris
   exit /B 0
)

if "%1"=="server" (
	echo Compiling server...
   javac -cp "lib/sqlite-jdbc-3.30.1.jar;." src/server/*.java 
   echo Running server...
	java -cp "lib/sqlite-jdbc-3.30.1.jar;." src/server/TetrisDatabase
	java -cp "lib/sqlite-jdbc-3.30.1.jar;." src/server/TetrisServer
   exit /B 0
)


::clean
if "%1"=="clean" (
   echo Cleaning directories...
   
   DEL /Q src\main\*.class >nul 2>&1
   DEL /Q src\server\*.class >nul 2>&1

   ::removing javadocs files
   DEL /Q docs\*.html docs\*.js docs\*.zip docs\*.css >nul 2>&1
   DEL /Q docs\element-list >nul 2>&1
   RMDIR /Q /S docs\jquery >nul 2>&1
   RMDIR /Q /S docs\resources >nul 2>&1
   RMDIR /Q /S docs\script-dir >nul 2>&1
   RMDIR /Q /S docs\src >nul 2>&1
   
   exit /B 0
)

echo Unknown command!
GOTO USAGE
