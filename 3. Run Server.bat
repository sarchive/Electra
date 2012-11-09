@echo off
title Running Electra Server
java -cp bin;lib\bcel-5.2.jar;lib\commons-compress-1.4.1.jar;lib\snakeyaml-1.11.jar;lib\events.jar net.electra.Server
pause