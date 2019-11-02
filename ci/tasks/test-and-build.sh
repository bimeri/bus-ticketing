#!/usr/bin/env bash

set -e
ls -l version/version
version=$(cat version/version)
echo $version
pwd
ls

export NG_CLI_ANALYTICS=ci
cd ui-source-code-from-gitlab/
mkdir -p gw_ui
npm install -g --save
npm install -g --save @angular-devkit/build-angular
npm install -g @angular/cli
ng build --output-path gw_ui
mv ./gw_ui/* ../source-code-from-gitlab/src/main/resources/static

cd ../source-code-from-gitlab/
echo "Current directory ..."
ls
echo "Packaging jar file"
./mvnw clean package
echo "Current directory ..."
ls
echo "copying jar to ../jar-file directory"
mv ./target/*.jar  ../jar-file/gowaka-$version.jar
ls -l ../jar-file
