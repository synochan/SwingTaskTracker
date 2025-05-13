#!/bin/bash

# Compile the movie adder
javac -cp .:bin:lib/* add_sample_movies.java

# Run the movie adder
java -cp .:bin:lib/* add_sample_movies

# Clean up the class file
rm add_sample_movies.class