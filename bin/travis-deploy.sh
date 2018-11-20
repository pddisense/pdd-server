#!/bin/bash
set -e

echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USERNAME" --password-stdin

bazel \
  --output_base=$HOME/.cache/bazel \
  --host_jvm_args=-Xmx500m \
  --host_jvm_args=-Xms500m \
  run \
  --config=ci\
   //pdd/java/ucl/pdd/server:publish

bazel \
  --output_base=$HOME/.cache/bazel \
  --host_jvm_args=-Xmx500m \
  --host_jvm_args=-Xms500m \
  run \
  --config=ci \
  //pdd/java/ucl/pdd/dashboard:publish
