# Camunda-ext

Repo with helper classes for BPMN development.
Included into [Latera Camunda docker image](https://hub.docker.com/r/latera/camunda).

## Download

See [Releases](https://github.com/latera/camunda-ext/releases) for .jar files and [Dockerhub](https://hub.docker.com/r/latera/camunda) for docker images.

## Documentation

See [Docs page](https://latera.github.io/camunda-ext/)

## Test Cases

See [Test reports page](ttps://latera.github.io/camunda-ext/test-reports)

## How to build

### Install [SDKman](https://sdkman.io/install)

```bash
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
```

### Install Java 8+, Groovy and Maven

```bash
sdk list java
sdk install java 8.0.232.hs-adpt #latest Java 8 version from previous command

sdk list groovy
sdk install groovy 2.4.16

sdk list maven
sdk install maven 3.6.0
```

### Clone this repo

```bash
git clone https://github.com/latera/camunda-ext.git
cd camunda-ext
```

### Build

```bash
./build.sh
```

See target/*.jar files

### Test

```bash
./test.sh
```

## How to run

```bash
cp .env.sample .env
docker-compose up -d
```

Then open http://localhost:8080/camunda with `user:changeme` credentials.
