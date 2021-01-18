#!/bin/bash

cd "$(dirname $0)"/..
mkdir -p log
./gradlew backend >log/backend.log 2>&1 &
echo $! > backend.pid
