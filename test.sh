#!/bin/bash
mvn clean compile package -DskipTests=false $@
