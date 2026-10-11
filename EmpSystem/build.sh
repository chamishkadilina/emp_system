#!/bin/sh
# Builds dist/EmpSystem.jar (plus dist/lib) without NetBeans. Needs JDK 17 or newer.
# Run the app afterwards with: java -jar dist/EmpSystem.jar
set -e
cd "$(dirname "$0")"

rm -rf build/classes dist
mkdir -p build/classes dist/lib

find src -name '*.java' > build/sources.txt
javac --release 17 -encoding UTF-8 -d build/classes -cp "lib/*" @build/sources.txt

# Copy images and other resources (everything except sources and NetBeans forms).
(cd src && find . -type f ! -name '*.java' ! -name '*.form' -exec sh -c \
    'mkdir -p "../build/classes/$(dirname "$1")" && cp "$1" "../build/classes/$1"' _ {} \;)

cp lib/*.jar dist/lib/
CLASS_PATH=$(cd dist && ls lib/*.jar | tr '\n' ' ')
printf 'Class-Path: %s\n' "$CLASS_PATH" > build/manifest.txt
jar --create --file dist/EmpSystem.jar --main-class empsystem.Main \
    --manifest build/manifest.txt -C build/classes .

echo "Built dist/EmpSystem.jar"
