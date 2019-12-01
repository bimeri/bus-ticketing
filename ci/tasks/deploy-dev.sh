#!/usr/bin/env bash

set -e
ls -l
#echo "${LDC_TARGET}:${LDC_USERNAME}@${LDC_PASSWORD}"

lc login -t $LDC_TARGET -u $LDC_USERNAME -p $LDC_PASSWORD
lc add-env GoWakaEnv -n APP_NAME -v $APP_NAME
lc add-env GoWakaEnv -n APP_CLIENT_ID -v $APP_CLIENT_ID
lc add-env GoWakaEnv -n APP_CLIENT_SECRET -v $APP_CLIENT_SECRET
lc add-env GoWakaEnv -n GW_DB_USER -v $GW_DB_USER
lc add-env GoWakaEnv -n GW_DB_PWD -v $GW_DB_PWD
lc get-env GoWakaEnv
lc push  --app-dir minio-s3/*.jar --config source-code-from-gitlab/ci/appconfig.json

