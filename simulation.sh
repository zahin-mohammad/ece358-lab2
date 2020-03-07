#!/bin/bash

./clean.sh
cd src/com/ece358 || exit
javac *.java
java Main
