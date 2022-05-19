#!/bin/bash

# Exodus
# Copyright 2022 Alberto Morón Hernández
# [github.com/albertomh/Exodus]
#
# New release
# ▔▔▔▔▔▔▔▔▔▔▔
# Run a full test suite and package into a JAR under `dist/`.
# Update badges in the README.md file.


SCRIPT_PATH=$(dirname $(realpath -s $0))
ROOT_DIR=$SCRIPT_PATH
# When invoked from a git hook, the context changes to the `.git/`
# directory. Use `pwd` instead when called by a git hook.
if [[ "$SCRIPT_PATH" == *".git"* ]]; then
    ROOT_DIR=$(pwd)
fi

get_version() {
    local pom_path="$ROOT_DIR/pom.xml"
    local pom_text=$(cat $pom_path)
    local version_re="<artifactId>exodus</artifactId>\s+<version>([0-9]\.[0-9]\.[0-9])</version>"
    if [[ $pom_text =~ $version_re ]]; then
        VERSION=${BASH_REMATCH[1]}
    fi
}

# ─────────────────────────────────────────────────────────────────────────────

VERSION=""

main() {
    printf "\n──────────── BEGIN NEW_RELEASE SCRIPT ─────────────\n"

    get_version

    printf "─────────────────────── END ───────────────────────\n\n"
}
main