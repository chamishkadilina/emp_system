@echo off
rem Builds dist\EmpSystem.jar (plus dist\lib) without NetBeans. Needs JDK 17 or newer.
rem Run the app afterwards with: java -jar dist\EmpSystem.jar
setlocal
cd /d "%~dp0"

if exist build\classes rmdir /s /q build\classes
if exist dist rmdir /s /q dist
mkdir build\classes
mkdir dist\lib

dir /s /b src\*.java > build\sources.txt
javac --release 17 -encoding UTF-8 -d build\classes -cp "lib/*" @build\sources.txt || exit /b 1

rem Copy images and other resources (everything except sources and NetBeans forms).
robocopy src build\classes /e /xf *.java *.form /njh /njs /nfl /ndl >nul
if errorlevel 8 exit /b 1

copy /y lib\*.jar dist\lib >nul
set CLASS_PATH=
for %%f in (lib\*.jar) do call set "CLASS_PATH=%%CLASS_PATH%% lib/%%~nxf"
echo Class-Path:%CLASS_PATH%> build\manifest.txt
jar --create --file dist\EmpSystem.jar --main-class empsystem.Main --manifest build\manifest.txt -C build\classes . || exit /b 1

echo Built dist\EmpSystem.jar
