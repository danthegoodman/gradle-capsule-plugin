# gradle-capsule-plugin

[![Version](http://img.shields.io/badge/Version-0.7.1-green.svg?style=flat-square)](https://github.com/danthegoodman/gradle-capsule-plugin/releases)
[![Capsule Version](http://img.shields.io/badge/Capsule%20Version-0.8.0-blue.svg?style=flat-square)](https://github.com/puniverse/capsule/releases)

A Gradle plugin for [Capsule], the packaging and deployment tool for JVM apps.

Capsule allows you to package your app and it's dependencies into a single jar for easy and efficient deployment.

This readme assumes some familiarity with the [Capsule] project.

[Capsule]:https://github.com/puniverse/capsule

# Adding the Plugin

```groovy
buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath 'us.kirchmeier:gradle-capsule-plugin:0.7.1'
    }
}

apply plugin: 'us.kirchmeier.capsule'
```


# Quick Start

A `FatCapsule` embeds your application and all dependencies into one jar.

A `ThinCapsule` contains your application and will download your dependencies on startup.

The `Capsule` task type is the core capsule provider and comes with almost no defaults.
You may use it to create an "empty capsule" that will download your entire application from a maven repository on startup.

```groovy
task fatCapsule(type: FatCapsule) {
  applicationClass 'com.foo.CoolCalculator'
}

task thinCapsule(type: ThinCapsule) {
  applicationClass 'com.foo.CoolCalculator'
}

task emptyCapsule(type: Capsule) {
  application 'com.foo:CoolCalculator:LATEST'
}
```


# Documentation

The `Capsule` tasks are all extentions upon the `Jar` task, with some additional configuration options available.

To build a capsule, one of the following properties must be defined:

* `applicationClass` - The Main class
* `application` - A maven dependency containing a main class
* `capsuleManifest.unixScript` - A startup script for unix machines
* `capsuleManifest.windowsScript` - A startup script for windows machines

## Task Defaults

By default, all capsules have the 'capsule' classifier and use the main implementation of the capsule library.

`FatCapsule` and `ThinCapsule` are task types which provide reasonable behavior with minimal configuration.
Aside from these default values, there is no distinction between them and the base `Capsule` task type.

```groovy
task fatCapsuleDefaults(type:FatCapsule){
  // Include the application's jar in the capsule
  applicationSource jar

  // Embed all runtime dependencies
  embedConfiguration = configurations.runtime

  // Limit the capsule library, since the dependencies are embedded
  capsuleFilter = { include 'Capsule.class' }
}

task thinCapsuleDefaults(type:ThinCapsule){
  // Include the application source in the capsule
  applicationSource sourceSets.main.outputs

  capsuleManifest {
    // Add all runtime dependencies as downloadable dependencies
    dependencyConfiguration = configurations.runtime
  }
}
```

## Manifest Options

`capsuleManifest` is a helper for defining the properties for configuring the capsule.
It is an instance of the [`CapsuleManifest`][src] class.

Please refer to the [source file][src] for a list of all possible properties.
Refer to the [Capsule] documentation for documentation on the properties.

[src]: https://github.com/danthegoodman/gradle-capsule-plugin/blob/master/src/main/groovy/us/kirchmeier/capsule/manifest/CapsuleManifest.groovy

```groovy
task myCapsule(type:ThinCapsule){
  applicationClass 'com.foo.CoolCalculator'

  capsuleManifest.systemProperties = ['java.awt.headless': true]
  capsuleManifest {
    repositories << 'jcenter'
  }
}
```

## Application Source

`applicationSource` defines how the application is brought into the capsule.

It is passed directly into a `from(...)` on the underlying implementation, so it may be a task, file, sourceset or more.

```groovy
task myCapsule(type:FatCapsule){
  applicationClass 'com.foo.FancyCalculator'
  applicationSource myFancyJar
}
```

## Embedding Jars

`embedConfiguration` defines which configuration contains the dependencies to embed.

```groovy
task myCapsule(type:FatCapsule){
  applicationClass 'com.foo.FancyCalculator'
  embedConfiguration configurations.runtime
}
```

## Downloadable Dependencies

`capsuleManifest.dependencyConfiguration` defines which configuration contains the dependencies to download on startup.

`capsuleManifest.dependencies` is a list of strings which are also downloaded on startup.
You may use this if you have a dependency you don't need gradle to care about.

```groovy
task myCapsule(type:ThinCapsule){
  applicationClass 'com.foo.BeautifulCalculator'
  capsuleManifest {
    dependencyConfiguration configurations.runtime
    dependencies << 'log4j:log4j:1.2.17'
  }
}
```

## "Really Executable" Capsules

`reallyExecutable` will make a capsule executable as a script in unix environments.
You may read more in the [capsule documentation][reallyexec].

`reallyExecutable.regular()` is the default and uses a plan execution script.
`reallyExecutable.trampoline()` will use the trompoline script.
`reallyExecutable.script(file)` may be set to define your own script.

[reallyexec]:https://github.com/puniverse/capsule#really-executable-capsules

```groovy
task executableCapsule(type:FatCapsule){
  applicationClass 'com.foo.CoolCalculator'
  reallyExecutable //implies regular()
}

task trampolineCapsule(type:ThinCapsule){
  applicationClass 'com.foo.CoolCalculator'
  reallyExecutable { trampoline() }
}

task myExecutableCapsule(type:FatCapsule){
  applicationClass 'com.foo.CoolCalculator'
  reallyExecutable {
    script file('my_script.sh')
  }
}
```

## Changing the capsule implementation

For advanced usage, `capsuleConfiguration` and `capsuleFilter` control where the capsule implementation comes from.
You may override them to change implementations, or set them to null and provide your own implemntation somehow else.
If you override these, you should also change the `capsuleManifest.mainClass` property.

By default for all Capsule types, `capsuleConfiguration` is set to `configurations.capsule`, which is provided by this plugin.

```groovy

configurations {
  myCapsule
}

dependencies {
  myCapsule 'com.foo:MyCapsuleImplementation:0.8'
}

task myCapsule(type: ThinCapsule){
  applicationClass 'com.foo.CoolCalculator'
  capsuleConfiguration configurations.myCapsule
}
```
