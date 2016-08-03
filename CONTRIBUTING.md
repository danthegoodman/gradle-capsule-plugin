# Contributing

Thank you for being willing to contribute!

## Running Tests

This project has unit tests for testing some of the written logic, and integration tests for validating against an example gradle project.

If you have some time on your hands and are running on a *nix machine, you can run the full test suite with:

    ./test.sh

But that command can take at least 5 minutes to run.
If you want to run a quicker and less comprehensive suite, just run the unit tests:

    ./gradlew check


## Updating the Capsule Version

The following locations should be checked for version references when upgrading the capsule version:

* `README.md`, in multiple links
* `CapsuleGradlePlugin.groovy`, in the dependencies block
