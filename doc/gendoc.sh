#!/bin/sh

BUILD_CMD="docker-compose build"
RUN_CMD="docker-compose run --rm sphinx"

case $1 in
html)
    echo "Building HTML doc"
    $RUN_CMD make html
    ;;

pdf)
    echo "Building PDF doc"
    $RUN_CMD make latexpdf
    ;;

all)
    echo "Building PDF and HTML doc"
    $RUN_CMD make html latexpdf
    ;;

clean)
    echo "Cleaning generated docs"
    $RUN_CMD make clean
    ;;

*)
  echo "USAGE: $0 html|pdf|all|clean"

esac
