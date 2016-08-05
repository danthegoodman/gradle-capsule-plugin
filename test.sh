#!/bin/bash

# Runs through a series of tests using multiple versions of gradle to
# ensure the plugin works in all of the expected versions.
#
# You can expect the tests to run for 20 to 30 seconds _per_ version.

VERSIONS=(
  2.14.1
  2.13
  2.12
  2.11
  2.10
  2.9
  2.8
  2.7
  2.6
  2.5
  2.4
)

main(){
  echo "## Building plugin and running unit tests"
  assemble_plugin || die "## Failed to build plugin"

  cd src/test/gradle
  for GRADLE_VERSION in ${VERSIONS[*]}; do
    echo "## $GRADLE_VERSION :: Testing plugin"
    write_wrapper_props_file "$GRADLE_VERSION"
    integration_tests || die "## $GRADLE_VERSION :: Tests failed"
  done

  echo "## All tests passed"
}

assemble_plugin(){
  ./gradlew clean check jar
}

integration_tests(){
  ./gradlew clean self-test
}

write_wrapper_props_file(){
  cat gradle/wrapper/gradle-wrapper.properties.base | \
    sed "s/gradle-[0-9.]*-bin.zip/gradle-$1-bin.zip/" \
    > gradle/wrapper/gradle-wrapper.properties
}

die(){
  echo "$1"
  exit 1
}

main