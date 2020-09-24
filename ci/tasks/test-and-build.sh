#!/usr/bin/env bash

set -e
ls -l version/version
version=$(cat version/version)
echo "${version}"
pwd
ls

cd source-code-from-gitlab/
echo "Current directory ..."
ls
echo "Packaging jar file"
./mvnw clean package
#./mvnw --settings ./settings.xml clean package
./mvnw --settings ../nexus-settings/settings.xml clean package
echo "Current directory ..."
ls
echo "copying jar to ../jar-file directory"
mv ./target/*.jar  ../jar-file/gowaka-"${version}".jar
ls -l ../jar-file
