@echo off
title Compiling...
cd "./Client/src"
"C:\Program Files\Java\jdk1.7.0_03\bin\javac.exe" -cp . -d ../bin/ ./*java
pause