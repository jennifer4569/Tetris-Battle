#!/bin/bash

#checks if we need to show help
show_help=0
if [ $# -eq 0 ]
then
    show_help=1
else
    if [ $1 != "compile" ] && [ $1 != "run" ]
    then
	if [ $1 != "clean" ] && [ $1 != "server" ]
	then
	    show_help=1
	fi
    fi
fi

#this line worked in VirtualBox but not in WSL
#if [ $# -eq 0 ] || [ $1 != "compile" ] && [ $1 != "run" ] && [ $1 != "test" ] && [ $1 != "clean" ] 
if [ $show_help -eq 1 ]
then
    if [ $# -eq 0 ]
    then
	echo "Please specify an argument!"
    else
	if [ $1 != "help" ]
	then
	    echo "Unknown command!"
	fi
    fi
    echo "NOTE: Please make sure that you're running this script under the homeworks/ directory!"
    echo "   * compile: compiles the java code and javadocs"
    echo "   * run: runs the program (assumes that you've compiled first)"
    echo "   * server: compiles and runs the server code"
    echo "   * help: lists all the possible commands (brings up this list)"
    echo "   * clean: cleans the directories"

else
    #compiles program and javadocs
    if [ $1 = "compile" ]
    then
        echo "Compiling program..."
        javac -cp "lib/sqlite-jdbc-3.30.1.jar:." src/main/*.java 
        echo "Compiling javadocs..."
        javadoc -cp "lib/sqlite-jdbc-3.30.1.jar:." -author -version -d docs src/main/*.java src/server/*.java
    fi

    #runs program
    if [ $1 = "run" ]
    then
	echo "Running program..."
	java -cp "lib/sqlite-jdbc-3.30.1.jar:." src/main/Tetris
    fi

    if [ $1 = "server" ]
    then
	echo "Compiling server..."
    javac -cp "lib/sqlite-jdbc-3.30.1.jar:." src/server/*.java 
    echo "Running server..."
	java -cp "lib/sqlite-jdbc-3.30.1.jar:." src/server/TetrisDatabase
	java -cp "lib/sqlite-jdbc-3.30.1.jar:." src/server/TetrisServer
    fi

    #clean
    if [ $1 = "clean" ]
    then
	echo "Cleaning directories..."
	rm -rf src/main/*.class
	rm -rf src/server/*.class
	
	#removing javadocs files
	rm -rf docs/*.html docs/*.js docs/*.zip docs/*.css
	rm -rf docs/jquery docs/resources docs/element-list
	rm -rf docs/script-dir docs/src
   
    fi
fi
