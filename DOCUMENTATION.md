# Plugin Documentation

This documentation is highly technical and expects familiarity with gradle and the [Capsule] project.

If you haven't already, check out the [README].

[Capsule]:http://www.capsule.io/
[README]:https://github.com/danthegoodman/gradle-capsule-plugin


## Type Heirarchy

`Capsule` is the base class for both `FatCapsule` and `MavenCapsule`.
It comes with almost no defaults, and is an ideal starting ground for advanced use cases.


## Jar Configuration

Because the capsule output is a jar, the Capsule task types extend the Jar type. 
You can use the standard jar methods for altering file names or controlling output.

See Also: [Gradle: Jar Tasks](http://gradle.org/docs/current/dsl/org.gradle.api.tasks.bundling.Jar.html)

```groovy
task simpleCapsule(type: FatCapsule){
  applicationClass 'com.foo.SimpleCalculator
  
  baseName 'SimpleCalculator'
}

task configuredCapsule(type: FatCapsule){
  applicationClass 'com.foo.ConfiguredCalculator'
  
  from 'preconfigration.properties'

  baseName 'Calculator'
  classifier 'configured' //overwrite the default: 'capsule'
}
```


## Manifest Attributes

`capsuleManifest` is a helper for defining the properties for configuring the capsule. 
With it, you may configure system properties, pass in arguments to your application, limit compatible JVM versions and more.

Technically, `applicationClass` is a manifest attribute. 
There base `applicationClass` property just applies the value into `capsuleManifest.applicationClass`.

See also: [Capsule: Manifest Attributes][manifest-cap] and [Source: `CapsuleManifest`][manifest-src]

[manifest-cap]:http://www.capsule.io/reference/#manifest-attributes
[manifest-src]:https://github.com/danthegoodman/gradle-capsule-plugin/blob/master/src/main/groovy/us/kirchmeier/capsule/manifest/CapsuleManifest.groovy

```groovy
task headlessCapsule(type:FatCapsule){
  applicationClass 'com.foo.CoolCalculator'

  capsuleManifest {
    systemProperties['java.awt.headless'] = true
  }
}

task slightlyFancyCapsule(type:MavenCapsule){
  capsuleManifest {
    applicationClass 'com.foo.FancyCalculator'
    args = ['--slightly-fancy']
  }
}

task veryFancyCapsule(type:MavenCapsule){
  applicationClass 'com.foo.FancyCalculator'

  capsuleManifest {
    args = ['--very-fancy']
    minJavaVersion = '1.8.0'
  }
}
```

## Caplets

Adding a `caplet` dependency will include the caplet inside your capsules. 
You will need to add the name of the caplet to `capsuleManifest.caplets`.
 
All capsule tasks have a default `capletConfiguration` pointing to the `caplet` dependency configuration.
Changing this value will allow you to configure caplets between different capsule tasks.

`MavenCapsule` technically has a default of `mavenCaplet`, which includes this maven capsule, but this configuration
extends the `caplet` dependency configuration.

See Also: [Capsule: What are caplets?](http://www.capsule.io/)

```
dependencies {
  caplet 'com.foo:capsule-awesome:1.0.0'
}

task capsule(type: MavenCapsule){
  applicationClass 'com.foo.CoolCalculator'
  
  capsuleManifest {
    caplets << 'AwesomeCapsule' //MavenCapsule already exists in this list at this point
  }
}

task capsule(type: FatCapsule){
  applicationClass 'com.foo.CoolCalculator'
  
  capsuleManifest {
    caplets << 'AwesomeCapsule'
  }
}

```

## Modes, OS specific and JVM specific options

`mode` blocks provide a mechanism for providing different operational modes.

`platform` blocks will apply a group of settings for specific platforms.
You can couple this with `applicationScript` for starting from a script instead of strictly a java class.

`java` blocks allow you to configure your application for specific JVM versions.

See Also: [Capsule: Modes and more](http://www.capsule.io/user-guide/#modes-platform--and-version-specific-configuration)

```groovy
task modeCapsule(type: FatCapsule){
  applicationClass 'com.foo.ComplexCalculator'
  
  capsuleManifest {
    mode('DEBUG'){
      systemProperties['log4j.debug'] = true
      capsuleLogLevel = 'DEBUG'
      systemProperties['com.foo.oauth_logging'] = true
      platform('windows'){
        //extra buggy on windows :(
        systemProperties['com.foo.ssh_logging'] = true
      }
    }
  }
}

task scriptCapsule(type: FatCapsule){
  capsuleManifest {
    systemProperties['log4j.configuration'] = 'log4j.xml'
    platform('windows'){
      applicationScript 'starter-win.bat'
    }
    platform('macos'){
      applicationScript 'starter-osx.sh'
      systemProperties['log4j.configuration'] = 'log4j-osx.xml'
    }
    platform('linux'){
      applicationScript 'starter-linux.sh'
    }
  }
}

task jvmSpecificCapsule(type: MavenCapsule){
  applicationClass 'com.foo.DateCalculator'
    
  capsuleManifest {
    java('8'){
      args << '--use-java-dates'
    }
    java('7'){
      dependencies << 'joda-time:joda-time:2.0'
      args << '--use-joda-dates'
    }
  }
}
```


## Chainging the Source from the `jar` task

`applicationSource` defines how the application is brought into the capsule. 

It is passed directly into [`from(...)`][gradle-jar-from], which accepts a variety of input types.

[gradle-jar-from]: http://gradle.org/docs/current/dsl/org.gradle.api.tasks.bundling.Jar.html#org.gradle.api.tasks.bundling.Jar:from(java.lang.Object[])

```groovy
task myFancyJar(type: Jar){
  /* ... */
}

task myCapsule(type:FatCapsule){
  applicationClass 'com.foo.FancyCalculator'
  applicationSource myFancyJar
}
```


## Embedding Dependencies

`embedConfiguration` defines which configuration contains the dependencies to embed.

The `FatCapsule` defaults to the `runtime` configuration. The `MavenCapsule` has no default. 

```groovy
task fancyCapsule(type:FatCapsule){
  applicationClass 'com.foo.FancyCalculator'
  embedConfiguration configurations.fancyRuntime
}
```


## Downloadable Dependencies

`capsuleManifest.dependencyConfiguration` defines which configuration contains the dependencies to download on startup.

`capsuleManifest.dependencies` is a list of strings which are also downloaded on startup.
You may use this if you have a dependency you don't need gradle to care about.

This setting applies only to the `MavenCapsule`. 
It defaults the dependencyConfiguration to the `runtime` configuration. 

```groovy
task beautifulCapsule(type:MavenCapsule){
  applicationClass 'com.foo.BeautifulCalculator'
  capsuleManifest {
    dependencyConfiguration configurations.beautifulRuntime
    dependencies << 'log4j:log4j:1.2.17'
  }
}
```


## "Really Executable" Capsules

`reallyExecutable` will make a capsule executable as a script in unix environments.

`reallyExecutable.regular()` is the default and uses a plan execution script.
`reallyExecutable.trampolining()` will use the trompoline script.
`reallyExecutable.script(file)` may be set to define your own script.

See More: [Capsule: Really Executable](http://www.capsule.io/user-guide/#really-executable-capsules)

```groovy
task executableCapsule(type:FatCapsule){
  applicationClass 'com.foo.HelloWorld'
  reallyExecutable //implies regular()
}

task trampolineCapsule(type:MavenCapsule){
  applicationClass 'com.foo.CoolCalculator'
  reallyExecutable { trampolining() }
}

task myExecutableCapsule(type:FatCapsule){
  applicationClass 'com.foo.SuperCalculator'
  reallyExecutable {
    script file('my_script.sh')
  }
}
```

Usage:

```text
$ gradle executableCapsule

$ ./build/libs/project-capsule.jar
Hello World!
```

## Using a Different Capsule Version

Preferrably, file an issue if the capsule version is out of date. 

If you really wish to use a different version of capsule, you may set it like so:
 
```groovy
project.capsule.version = '1.1'
```

This will apply the specified version to the following configurations:

* `capsule`
* `capsuleUtil`
* `mavenCaplet`
