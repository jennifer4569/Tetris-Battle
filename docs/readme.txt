======================================================================================================================
=-------------------------------------------------    README.TXT    -------------------------------------------------=
======================================================================================================================

======================================================================================================================
DESCRIPTION
======================================================================================================================
   We implemented Tetris Battle, a PvP version of the classic video game Tetris. The game involves trying to 
   achieve the highest possible score, while simultaneously trying to survive longer than your opponent. 
   Additionally, on top of attempting to outlast your opponent, you also have the ability to make the game 
   harder for your opponent by adding partially complete rows to their grid, which could lead to their 
   disqualification.

   There is be a central server (67.205.133.16), which accepts up to 32 clients at once. The central server 
   authenticates the users’ login, and stores various statistics about each user, as well as a leaderboard. 
   It also automatically performs matchmaking for each user who wishes to play.

   NOTE: To be able to run the program, you must have JDK installed.

======================================================================================================================
HOW TO USE THE SCRIPT
======================================================================================================================
   NOTE: To properly run the script, you must be in the Tetris-Battle/ directory.
   
   To run the script with a specific command, run:
      - On Windows: ./scripts/script.bat <command>
      - On Mac/Linux: ./scripts/script.sh <command>

   The following list is also available when running:
      - On Windows: ./scripts/script.bat help
      - On Mac/Linux: ./scripts/script.sh help
    
======================================================================================================================
SCRIPT COMMANDS
======================================================================================================================
   NOTE: Please make sure that you're running this script under the Tetris-Battle/ directory!
     * compile: compiles the java code and javadocs
     * run: runs the program (assumes that you've compiled first)
     * server: compiles and runs the server code
     * help: lists all the possible commands (brings up this list)
     * clean: cleans the directories

======================================================================================================================
EXTRA DIRECTORIES
======================================================================================================================
   - lib/ contains files needed for the SQL database, required for the server code. 

======================================================================================================================
DEVELOPMENT ENVIRONMENT
======================================================================================================================
Jennifer:
   I primarily used Git Bash on Windows to develop this program, but also used Ubuntu 18.04.4 via my Virtual 
   Box and Powershell on my Windows 10 laptop to test my program. To test the server/client connection, I 
   also used WSL, and a DigitalOcean droplet (Ubuntu 20.04 (LTS) x64).
   
Michael:
   Written in: WSL for Windows 10 version 2004

    Ubuntu version: 18.04.2

    Powershell information:
    Major   Minor   Build   Revision
    --------------------------------
    5       1       18362   752