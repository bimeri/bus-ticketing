#!/usr/bin/env bash

set -e
ls -l
#echo "${LDC_TARGET}:${LDC_USERNAME}@${LDC_PASSWORD}"

ldc login -t $LDC_TARGET -u $LDC_USERNAME -p $LDC_PASSWORD
ldc push  --app-dir minio-s3/*.jar --config source-code-from-gitlab/ci/appconfig.json

