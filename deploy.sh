#!/bin/bash

# Save current dir as root
export ROOT_DIR=$(pwd)

# Copy all JAR dependencies to ./target/dependencies
mvn dependency:copy-dependencies -U

# Build demo processes
cd ./demo_processes && find ./ -type d -maxdepth 1 -mindepth 1 -exec bash -c "cd {} && ./build.sh" ';'
cd $ROOT_DIR

docker build -t $DOCKERHUB_REPO:$TRAVIS_JOB_ID -f Dockerfile .
