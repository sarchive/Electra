@echo off
title Running Electra Compiler
java -cp bin;lib\bcel-5.2.jar;lib\snakeyaml-1.11.jar net.electra.compiler.Compiler
pause