#!/bin/sh

addMount() {
    local src=$(echo "$1" | awk '{split($0,a,":"); print a[1]}')
    local dst=$(echo "$1" | awk '{split($0,a,":"); print a[2]}')
    src=$(realpath $src)
    MOUNT_OPTS="$MOUNT_OPTS --mount=type=bind,source=$src,destination=$dst"
}


# adjust version to point to a specific release of the image
IMG_VERSION="1.1"
IMAGE="public.docker.ecsec.de/ecsec/tools/sphinx:$IMG_VERSION"
# format of MOUNTS is "<src1>:<dest1> <src2>:<dest2>"
MOUNTS="..:/src"


# create mount options
for m in $MOUNTS; do
    addMount $m
done

# replace docker with podman if possible
DOCKER="docker"
if [ -f "$(which podman)" ]; then
    DOCKER="podman"
fi

RUN_CMD="$DOCKER run -w /src/doc -it --rm $MOUNT_OPTS $IMAGE"

for arg in $@; do
    case $arg in
	init)
	    echo "Generating build files"
	    $RUN_CMD sphinx-quickstart
	    ;;
	html)
	    echo "Building HTML doc"
	    $RUN_CMD make html
	    ;;

	pdf)
	    echo "Building PDF doc"
	    $RUN_CMD make latexpdf
	    ;;

	clean)
	    echo "Cleaning generated docs"
	    $RUN_CMD make clean
	    ;;

	*)
	    echo "USAGE: $0 (init|html|pdf|clean)+"
	    echo "ERROR: Unknown argument '$arg'"

    esac
done
