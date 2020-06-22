#!/bin/bash

CAMUNDA_EXT_ENV=test mvn clean compile package -DskipTests=false
