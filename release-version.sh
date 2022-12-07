#!/usr/bin/env sh

pwd="$(dirname $0)"
build_all_profiles="build-mobile-libs,desktop-package"
push_changes=true

$pwd/mvnw release:prepare \
	-DautoVersionSubmodules=true \
	-DpushChanges=$push_changes \
	-DsignTag=true \
	-DpreparationGoals="clean" \
	-P "$build_all_profiles"

$pwd/mvnw release:perform \
	-DdryRun \
	-DlocalCheckout=true \
	-P "$build_all_profiles"
