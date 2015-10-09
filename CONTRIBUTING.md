# Contributing

Thank you for being willing to contribute!

## Running Tests

This project has unit tests for testing some of the written logic, and integration tests for validating against an example gradle project.

If you make changes to the code, you can run all of the tests with:

    ./gradlew check

If you feel brave and are running on a *nix machine, you can run additional tests via:

    ./integration_test.sh

## Updating the Capsule Version

The following locations should be checked for version references when upgrading the capsule version:

* `README.md`, in multiple links
* `CapsuleGradlePlugin.groovy`, in the dependencies block
