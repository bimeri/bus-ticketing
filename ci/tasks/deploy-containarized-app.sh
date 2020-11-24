#!/usr/bin/env bash
set -e
pwd
ls -l

appName=$LCS_APPNAME
appType=$LCS_APPTYPE
port=$LCS_PORT
version=$LCS_VERSION
registry=$LCS_REGISTRY
username=$LCS_USERNAME
password=$LCS_PASSWORD
url=$LCS_URL

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

curl -v -F 'appfile=@minio-s3/*.jar' -F 'config=@./config.json' "${url}"
