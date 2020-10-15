#!/bin/sh

addMount() {
    local src=$(echo "$1" | awk '{split($0,a,":"); print a[1]}')
    local dst=$(echo "$1" | awk '{split($0,a,":"); print a[2]}')
    src=$(realpath $src)
    MOUNT_OPTS="$MOUNT_OPTS --mount=type=bind,source=$src,destination=$dst"
}


IMG_VERSION="1.1"
IMAGE="public.docker.ecsec.de/ecsec/tools/sphinx:$IMG_VERSION"
MOUNTS=".:/docs ..:/src"


# create mount options
for m in $MOUNTS; do
    addMount $m
done

# replace docker with podman if possible
DOCKER="docker"
if [ -f "$(which podman)" ]; then
    DOCKER="podman"
fi

RUN_CMD="$DOCKER run -it --rm $MOUNT_OPTS $IMAGE"

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
