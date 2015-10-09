# gradle-capsule-plugin

A Gradle plugin for [Capsule], the packaging and deployment tool for JVM apps.

[Capsule]:http://www.capsule.io/

# Adding the Plugin

This plugin requires Gradle 2.4 and above.
Simply define the plugins at the top of your build script:

```groovy
plugins {
  id "us.kirchmeier.capsule" version "1.0.1"
}
```

Gradle 2.0 to 2.3 were last supported with [plugin version 1.0.0](https://plugins.gradle.org/plugin/us.kirchmeier.capsule/1.0.0).

# Quick Start

This plugin defines no tasks, but instead provides several task types for building your own tasks.


## FatCapsule

The `FatCapsule` type embedds your application and all of its dependencies into one executable jar file:

```groovy
task fatCapsule(type: FatCapsule) {
  applicationClass 'com.foo.HelloWorld'
}
```

Use it like so:

``` text
$ gradle fatCapsule

$ cd build/libs
$ ls
project.jar project-capsule.jar

$ java -jar project-capsule.jar
Hello World!
```

Without further configuration:

* It obtains your project source from the `jar` task. 
* It will include all of the dependencies from the `runtime` dependency configuration.
* It will have the `capsule` classifier in it's file name.


## MavenCapsule

The `MavenCapsule` type embeds only your application. It will download dependencies when the user executes the capulse.

Under the hood, it uses the [maven capsule][mvn-cap], which caches dependencies after downloading them.

[mvn-cap]: https://github.com/puniverse/capsule-maven/tree/v1.0.0

```groovy
task mavenCapsule(type: MavenCapsule){
  applicationClass 'com.foo.CoolCalculator'
}
```

Without further configuration:

* It obtains your project source from the `jar` task. 
* It will download at runtime all of the dependencies from the `runtime` dependency configuration.
* It will have the `capsule` classifier in it's file name.


## Configuration

Capsule allows you define attributes in your capsule to onfigure system properties, pass in arguments to your application, limit compatible JVM versions and more.
You may configure these using the `capsuleManifest` block.

See also: [Capsule: Manifest Attributes][manifest-cap] and [Source: `CapsuleManifest`][manifest-src]

[manifest-cap]:http://www.capsule.io/reference/#manifest-attributes
[manifest-src]:https://github.com/danthegoodman/gradle-capsule-plugin/blob/master/src/main/groovy/us/kirchmeier/capsule/manifest/CapsuleManifest.groovy

```groovy
task myCapsule(type:FatCapsule){
  applicationClass 'com.foo.FancyCalculator'

  capsuleManifest {
    systemProperties['log4j.configuration'] = 'log4j.xml'
    args = ['--very-fancy']
    minJavaVersion = '1.8.0'
  }
}
```


# Documentation

More thorough documentation is available in [`DOCUMENTATION.md`][docs].

[docs]:https://github.com/danthegoodman/gradle-capsule-plugin/blob/master/DOCUMENTATION.md


# Support

If you run into any issues or have an enhancement idea, please [file an issue](https://github.com/danthegoodman/gradle-capsule-plugin/issues).

If you have any questions, capsule or gradle related, please start a topic on the [Google Group/Mailing List][group].

[group]:https://groups.google.com/forum/#!forum/capsule-user
