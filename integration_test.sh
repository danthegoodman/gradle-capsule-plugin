#!/bin/bash

# Runs through a series of tests using multiple versions of gradle to
# ensure the plugin works in all of the expected versions.
#
# Requires http://sdkman.io/ to be installed.

VERSIONS=(2.{10..4})

export SDKMAN_DIR=~/.sdkman
[[ -s "$SDKMAN_DIR/bin/sdkman-init.sh" ]] && source "$SDKMAN_DIR/bin/sdkman-init.sh"

./gradlew clean jar

cd src/test/gradle

RESULT=0
for grdlVersion in ${VERSIONS[*]}; do
  sdk install gradle $grdlVersion
  sdk use gradle $grdlVersion
  gradle clean self-test
  RESULT+=$?
done
exit $RESULT
