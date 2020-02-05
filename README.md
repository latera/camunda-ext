[![Release](https://travis-ci.com/latera/camunda-ext.svg?branch=master)](https://travis-ci.com/latera/camunda-ext)
[![JAR](https://jitpack.io/v/latera/camunda-ext.svg)](https://jitpack.io/#latera/camunda-ext)
# Camunda-ext

Repo with helper classes for BPMN development.
Included into [Latera Camunda docker image](https://hub.docker.com/r/latera/camunda).

# Download
See [Releases](https://github.com/latera/camunda-ext/releases) for .jar files and [Dockerhub](https://hub.docker.com/r/latera/camunda) for docker images.

# How to build
## Install [SDKman](https://sdkman.io/install)
```bash
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
```
## Install Java 8+, Groovy 2.4.12 and Ant
```bash
sdk list java
sdk install java 8.0.232.hs-adpt #latest Java 8 version from previous command

sdk list groovy
sdk install groovy 2.4.12

sdk list ant
sdk install ant 1.10.1
```
## Clone this repo
```bash
git clone https://github.com/latera/camunda-ext.git
cd camunda-ext
```
## Build
```bash
ant -f build.xml
```
See target/*.jar files

# How to run
```bash
cp .env.sample .env
docker-compose up -d
```
Then open http://localhost:8080/camunda and create new admin user here.


