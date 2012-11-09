@echo off
title Compiling Electra Server
dir /s /B *.java > sources.txt
"C:\Program Files\Java\jdk1.7.0_03\bin\javac.exe" -classpath src;lib\events.jar;lib\commons-compress-1.4.1.jar;lib\bcel-5.2.jar;lib\snakeyaml-1.11.jar -d bin @sources.txt
del sources.txt
pause