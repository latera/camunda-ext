[![Release](https://jitpack.io/v/latera/camunda-ext.svg)](https://jitpack.io/#latera/camunda-ext)
# Camunda-ext

Repo with helper classes for BPMN development.
Included into [Latera Camunda docker image](https://github.com/latera/camunda-docker).

# Documentation
See https://latera.github.io/camunda-ext/

# Test Cases
See https://latera.github.io/camunda-ext/test-reports

# How to build
## Install [SDKman](https://sdkman.io/install)
```bash
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
```
## Install Java 8+, Groovy and Maven
```bash
sdk install java 8.0.222
sdk install groovy 2.4.16
sdk install maven 3.6.0
```
## Clone this repo
```bash
git clone https://github.com/latera/camunda-ext.git
cd camunda-ext
```
## Build
```bash
./build.sh
```
See target/*.jar files