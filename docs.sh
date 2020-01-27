#!/bin/bash

# Insall gh-pages-multi
npm i -g @koumoul/gh-pages-multi

export PROJECT="camunda-ext"

export VERSION=
if [[ "x$TRAVIS_TAG" != "x" ]]; then
  export VERSION="$TRAVIS_TAG"
fi

if [[ "$TRAVIS_BRANCH" == "master" ]]; then
  export VERSION=latest
fi

# Generate docs
mvn compile groovydoc:generate com.bluetrainsoftware.maven:groovydoc-maven-plugin:2.1:attach-docs $@

# If this is a Travis build, use Github Pages relative urls /camunda-ext/version
# Otherwise use just /camunda-ext without version,
# In most cases you use latest commit in branch like master (=unreleased version, e.g. 1.5), and version from just pom.xml (1.4) is not consistent with that
if [[ "x$VERSION" != "x" ]]; then
  export DOC_BASE_URL="/$PROJECT/$VERSION"
else
  export DOC_BASE_URL="/$PROJECT"
fi

# Replace $docBaseUrl in doc urls
unamestr=`uname`
if [[ "$unamestr" == "Linux" ]]; then
  find docs -type f -name "*.html" -exec sed -i -E "s#\\$\{docBaseUrl\}#$DOC_BASE_URL#g" {} ';'
else
  find docs -type f -name "*.html" -exec ex -sc "s#\\$\{docBaseUrl\}#$DOC_BASE_URL#g" -cx {} ';'
fi

# Push docs into separated dirs in gh-pages repo
if [[ "x$VERSION" != "x" ]]; then
  git remote remove origin
  git remote add origin https://${GITHUB_TOKEN}@github.com/$TRAVIS_REPO_SLUG.git > /dev/null 2>&1

  gh-pages-multi deploy --no-history -t $VERSION
fi
