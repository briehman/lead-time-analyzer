#!/bin/bash

cd "$(dirname $0)"/..
if [[ -f backend.pid ]]; then
  kill $(cat backend.pid)
  rm backend.pid
else
  echo "ERROR: backend.pid does not exist" >&2
fi
