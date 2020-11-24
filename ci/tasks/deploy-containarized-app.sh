#!/usr/bin/env bash
set -e
pwd
ls -l
ls minio-s3

appName=$LCS_APPNAME
appType=$LCS_APPTYPE
port=$LCS_PORT
version=$(cat version/version)
registry=$LCS_REGISTRY
username=$LCS_USERNAME
password=$LCS_PASSWORD
url=$LCS_URL
appFile=minio-s3/gowaka-service-*.jar

echo "${version}"

echo "{
  \"appName\": \"${appName}\",
  \"appType\": \"${appType}\",
  \"password\": \"${password}\",
  \"port\": ${port},
  \"registry\": \"${registry}\",
  \"username\": \"${username}\",
  \"version\": \"${version}\"
}" >./config.json

curl -v -F "appfile=@${appFile}" -F 'config=@./config.json' "${url}"
