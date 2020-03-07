#!/bin/bash

./clean.sh
cd src || exit
javac *.java
java Main
