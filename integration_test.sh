#!/bin/bash

# Runs through a series of tests using multiple versions of gradle to
# ensure the plugin works in all of the expected versions.
#
# Requires http://sdkman.io/ to be installed.

VERSIONS=(2.{7..4})

export SDKMAN_DIR=~/.sdkman
[[ -s "/Users/danny/.sdkman/bin/sdkman-init.sh" ]] && source "/Users/danny/.sdkman/bin/sdkman-init.sh"

cd src/test/gradle

for grdlVersion in ${VERSIONS[*]}; do
  sdk use gradle $grdlVersion
  gradle clean self-test
done




