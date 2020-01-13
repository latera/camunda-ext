#!/bin/bash

export TAG=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
export PREV_DIR=$(pwd)

cd ..
git clone https://github.com/latera/camunda-ext.git master
cd master
export LATEST_MASTER_COMMIT=$(git log -n1 --pretty=format:'%H')
cd $PREV_DIR

export BRANCH="$TRAVIS_BRANCH"
if [[ "$LATEST_MASTER_COMMIT" == "$TRAVIS_COMMIT" ]]; then
  export BRANCH=master
fi

export MESSAGE="Triggered by camunda-ext"
if [[ "x$BRANCH" != "xmaster" ]]; then
  export MESSAGE="$MESSAGE from separated branch $BRANCH"
fi

#export LATEST_TAG_COMMIT="$(git rev-list -n 1 $TAG)"
#if [[ "x$TAG" != "x" && "x$TRAVIS_COMMIT" != "x$LATEST_TAG_COMMIT" ]]; then
#  git tag -d $TAG
#  git tag $TAG
#  git remote add origin-travis https://${GITHUB_TOKEN}@github.com/$TRAVIS_REPO_SLUG.git > /dev/null 2>&1
#  git push --tags -f --set-upstream origin-travis --quiet
#  exit 0
#fi

curl -s -X POST \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json' \
  -H 'Travis-API-Version: 3' \
  -H "Authorization: token ${TRAVIS_TOKEN}" \
  -d "{\"request\":{\"branch\":\"$BRANCH\",\"message\":\"$MESSAGE\",\"config\":{\"merge_mode\":\"deep_merge\",\"env\":{\"VERSION\":\"$TAG\",\"COMMIT\":\"$COMMIT\"}}}}" \
  https://api.travis-ci.com/repo/$DOCKER_REPO/requests || exit 0
