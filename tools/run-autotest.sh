#!/bin/bash
# runs the dev client with run/tctest.properties (see train.devtest.AutoTest); log to $1
cd ~/src/tc-neo
timeout ${TIMEOUT:-900} ./gradlew runClient --console=plain > "$1" 2>&1
echo EXIT $? >> "$1"
