#!/usr/bin/env bash

set -e
ls -l
#
#lc login -t $LDC_TARGET -u $LDC_USERNAME -p $LDC_PASSWORD
#lc add-env GoWakaEnv -n APP_NAME -v $APP_NAME
#lc add-env GoWakaEnv -n GW_DB_NAME -v $GW_DB_NAME
#lc add-env GoWakaEnv -n APP_CLIENT_URL -v $APP_CLIENT_URL
#lc add-env GoWakaEnv -n APP_CLIENT_ID -v $APP_CLIENT_ID
#lc add-env GoWakaEnv -n APP_CLIENT_SECRET -v $APP_CLIENT_SECRET
#lc add-env GoWakaEnv -n GW_DB_USER -v $GW_DB_USER
#lc add-env GoWakaEnv -n GW_DB_PWD -v $GW_DB_PWD
#lc add-env GoWakaEnv -n PAYAMGO_CLIENT_SECRET -v $PAYAMGO_CLIENT_SECRET
#lc add-env GoWakaEnv -n PAYAMGO_CLIENT_KEY -v $PAYAMGO_CLIENT_KEY
#lc add-env GoWakaEnv -n AS_PUBLIC_KEY -v $AS_PUBLIC_KEY
#lc get-env GoWakaEnv

lc push --app-file minio-s3/*.jar --config $PWD/source-code-from-gitlab/ci/config.yml --env "APP_NAME=${APP_NAME},GW_DB_NAME=$GW_DB_NAME,APP_CLIENT_URL=${APP_CLIENT_URL},APP_CLIENT_ID=${APP_CLIENT_ID},APP_CLIENT_SECRET=${APP_CLIENT_SECRET},GW_DB_USER=${GW_DB_USER},GW_DB_PWD=${GW_DB_PWD},PAYAMGO_CLIENT_SECRET=${PAYAMGO_CLIENT_SECRET},PAYAMGO_CLIENT_KEY=${PAYAMGO_CLIENT_KEY},AS_PUBLIC_KEY=${AS_PUBLIC_KEY}"

