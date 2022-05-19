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

package_release() {
    printf "\nPackaging into 'dist/exodus-$VERSION.jar'\n\n"
    ./mvnw --quiet clean package
    cp ./target/*.jar ./dist
}

# Badges:  version
set_badges_in_readme() {
    printf "\nSetting badges in README.\n"
    local readme_path="$ROOT_DIR/README.md"

    # Set version badge.
    local version_badge_re='\/badge\/version.+?white'
    local new_version_badge="\/badge\/version-${VERSION}-white"
    sed -i -E "s/$version_badge_re/$new_version_badge/g" "$readme_path"
    printf "    Set badge ( version | ${VERSION} )\n"
}

cleanup() {
    printf "\nRemoving '/target'\n"
    rm -rf "$ROOT_DIR/target/"
}

# ─────────────────────────────────────────────────────────────────────────────

VERSION=""

main() {
    printf "\n──────────── BEGIN NEW_RELEASE SCRIPT ─────────────\n"

    get_version

    package_release

    set_badges_in_readme

    cleanup

    printf "\n─────────────────────── END ───────────────────────\n\n"
}
main