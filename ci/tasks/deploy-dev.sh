#!/usr/bin/env bash

set -e
ls -l
#echo "${LDC_TARGET}:${LDC_USERNAME}@${LDC_PASSWORD}"

ldc login -t $LDC_TARGET -u $LDC_USERNAME -p $LDC_PASSWORD
ldc add-env GoWakaEnv -n APP_NAME -v $APP_NAME
ldc add-env GoWakaEnv -n APP_CLIENT_ID -v $APP_CLIENT_ID
ldc add-env GoWakaEnv -n APP_CLIENT_SECRET -v $APP_CLIENT_SECRET
ldc get-env GoWakaEnv
ldc push  --app-dir minio-s3/*.jar --config source-code-from-gitlab/ci/appconfig.json

