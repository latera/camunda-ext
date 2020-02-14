#!/bin/bash

# Save current dir as root
export ROOT_DIR=$(pwd)

# Copy all JAR dependencies to ./target/dependencies
mvn dependency:copy-dependencies -U

# Build seed
cd ./seed && ./build.sh
cd $ROOT_DIR

# Build demo processes
cd ./demo_processes && find ./ -type d -maxdepth 1 -mindepth 1 -exec bash -c "cd {} && ./build.sh" ';'
cd $ROOT_DIR

# Get demo processes list
cd ./demo_processes && find ./ -type d -maxdepth 1 -mindepth 1 -exec bash -c "cd {} && echo \$(basename \$(pwd))" ';' > $ROOT_DIR/demo_processes/list
cd $ROOT_DIR

if [[ "x$DOCKERHUB_REPO" != "x" ]]; then
  export REPO="$DOCKERHUB_REPO"
else
  export REPO="latera/camunda"
fi

export VERSION=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
export REVISION=$(mvn buildnumber:create help:evaluate -Dexpression=buildNumber -q -DforceStdout)

if [[ "x$TRAVIS_JOB_ID" != "x" ]]; then
  docker build --label "org.opencontainers.image.created"="$(date --rfc-3339=seconds)" --label "org.opencontainers.image.revision"="$REVISION" --label "org.opencontainers.image.version"="$VERSION" -t $DOCKERHUB_REPO:$TRAVIS_JOB_ID -f Dockerfile .
else
  docker build --label "org.opencontainers.image.created"="$(date --rfc-3339=seconds)" --label "org.opencontainers.image.revision"="$REVISION" --label "org.opencontainers.image.version"="$VERSION" -t $REPO:$VERSION -f Dockerfile .
fi
