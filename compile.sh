#!/bin/bash

# Create necessary directories if they don't exist
mkdir -p bin
mkdir -p db
mkdir -p logs
mkdir -p data
mkdir -p temp

# Clean previous compilation
rm -rf bin/*

echo "Compiling Java files..."

# Compile all Java files from the base directory
javac -d bin -cp .:lib/* $(find src -name "*.java")

if [ $? -eq 0 ]; then
    echo "Compilation successful!"
    echo "Creating classpath with libraries..."
    
    # Build classpath with all JARs in lib directory
    CLASSPATH="bin"
    for jar in lib/*.jar; do
        CLASSPATH="$CLASSPATH:$jar"
    done
    
    echo "Running CineBook CDO application..."
    java -cp $CLASSPATH com.cinebook.Main
else
    echo "Compilation failed!"
fi