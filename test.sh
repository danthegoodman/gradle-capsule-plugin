#!/bin/bash

# Runs through a series of tests using multiple versions of gradle to
# ensure the plugin works in all of the expected versions.
#
# You can expect the tests to run for 20 to 30 seconds _per_ version.

set -eu

VERSIONS=(
    5.0
    4.10
    4.9
    4.8
    4.7
    4.6
    4.5
    4.4
    4.3
)

main(){
  echo "## Building plugin and running unit tests"
  assemble_plugin

  cd src/test/gradle
  for GRADLE_VERSION in ${VERSIONS[*]}; do
    echo
    echo "###################### $GRADLE_VERSION ######################"
    write_wrapper_props_file "$GRADLE_VERSION"
    integration_tests
  done

  echo "## All tests passed"
}

assemble_plugin(){
  ./gradlew clean check jar
}

integration_tests(){
  cp really-executable.base.sh really-executable.sh
  echo "## Building and verifying capsules"
  ./gradlew clean buildAll runScript -PtestScript=TestCapsule.groovy

  echo "## Modifying some files and rebuilding"
  cp really-executable.modified.sh really-executable.sh
  ./gradlew buildAll runScript -PtestScript=TestModifications.groovy
}

write_wrapper_props_file(){
  cat gradle/wrapper/gradle-wrapper.properties.base | \
    sed "s/gradle-[0-9.]*-bin.zip/gradle-$1-bin.zip/" \
    > gradle/wrapper/gradle-wrapper.properties
}

main
