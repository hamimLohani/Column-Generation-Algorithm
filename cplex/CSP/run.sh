#!/bin/bash

CPLEX_JAR="/Users/Inz_mac/Applications/CPLEX_Studio_Community2212/cplex/lib/cplex.jar"
CPLEX_LIB_PATH="/Users/Inz_mac/Applications/CPLEX_Studio_Community2212/cplex/bin/arm64_osx"

if [ ! -f "$CPLEX_JAR" ]; then
    echo "Error: cplex.jar not found at: $CPLEX_JAR"
    echo "Please edit 'run.sh' and update the CPLEX_JAR path."
    exit 1
fi

if [ ! -d "$CPLEX_LIB_PATH" ]; then
    echo "Warning: CPLEX library path not found at: $CPLEX_LIB_PATH"
    echo "The application might fail if it cannot find native libraries."
fi

mkdir -p bin

echo "Compiling sources..."
javac -d bin -cp ".:$CPLEX_JAR" \
    src/util/*.java \
    src/model/*.java \
    src/master/*.java \
    src/pricing/*.java \
    src/cg/*.java \
    src/Main.java

if [ $? -ne 0 ]; then
    echo "Compilation failed."
    exit 1
fi

echo "Compilation successful."

echo "Running Crew Scheduling CLI..."
echo "------------------------------------------------"
java --enable-native-access=ALL-UNNAMED -cp "bin:$CPLEX_JAR" -Djava.library.path="$CPLEX_LIB_PATH" Main
