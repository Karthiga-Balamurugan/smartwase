@echo off
cd /d "%~dp0"
javac -cp "lib/sqlite-jdbc-3.53.4.0.jar" *.java
if errorlevel 1 (
  echo Compilation failed. Check the error messages above.
  pause
  exit /b 1
)
java -cp ".;lib/sqlite-jdbc-3.53.4.0.jar" Main
pause
