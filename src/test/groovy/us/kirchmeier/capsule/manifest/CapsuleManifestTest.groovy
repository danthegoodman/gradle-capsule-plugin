package us.kirchmeier.capsule.manifest

import org.gradle.testfixtures.ProjectBuilder
import org.testng.annotations.Test
import us.kirchmeier.capsule.manifest.CapsuleManifest;

public class CapsuleManifestTest {

  @Test
  public void "build manifest attributes with application"() {
    def m = new CapsuleManifest()
    m.application = 'foo:bar'
    def result = m.buildAllManifestAttributes()

    assert [(null):['Main-Class': 'Capsule', 'Application': 'foo:bar', 'Premain-Class': 'Capsule']] == result
  }

  @Test
  public void "build nested java manifest attributes with application"() {
    def m = new CapsuleManifest()
    m.java(7){ application = 'foo:bar:j7' }
    m.java(8){ application = 'foo:bar:j8' }
    m.java(9){ application = 'foo:bar:j9' }
    def result = m.buildAllManifestAttributes()

    assert result == [
        (null):['Main-Class': 'Capsule', 'Premain-Class': 'Capsule'],
        'java-7': ['Application': 'foo:bar:j7'],
        'java-8': ['Application': 'foo:bar:j8'],
        'java-9': ['Application': 'foo:bar:j9'],
    ]
  }

  @Test
  public void "build manifest attributes with applicationClass"() {
    def m = new CapsuleManifest()
    m.applicationClass = 'foo.bar'
    def result = m.buildAllManifestAttributes()

    assert [(null):['Main-Class': 'Capsule', 'Application-Class': 'foo.bar', 'Premain-Class': 'Capsule']] == result
  }

  @Test
  public void "build nested mode manifest attributes with applicationClass"() {
    def m = new CapsuleManifest()
    m.applicationClass = 'foo.main'
    m.mode('beta') { applicationClass = 'foo.beta' }
    m.mode('gamma') { applicationClass = 'foo.gamma' }
    def result = m.buildAllManifestAttributes()

    assert result == [
        (null):['Main-Class': 'Capsule', 'Application-Class': 'foo.main', 'Premain-Class': 'Capsule'],
        'beta':['Application-Class': 'foo.beta'],
        'gamma':['Application-Class': 'foo.gamma'],
    ]
  }

  @Test
  public void "build manifest attributes with applicationScript"() {
    def m = new CapsuleManifest()
    m.applicationScript = 'foo.sh'
    def result = m.buildAllManifestAttributes()

    assert [(null):['Main-Class': 'Capsule', 'Application-Script': 'foo.sh', 'Premain-Class': 'Capsule']] == result
  }

  @Test
  public void "build nested platform manifest attributes with applicationScript"() {
    def m = new CapsuleManifest()
    m.platform('Windows') { applicationScript = 'foo.bat' }
    m.platform('MACOS') { applicationScript = 'foo-osx.sh' }
    m.platform('linux') { applicationScript = 'foo-lin.sh' }
    def result = m.buildAllManifestAttributes()

    assert result == [
        (null):['Main-Class': 'Capsule', 'Premain-Class': 'Capsule'],
        'windows':['Application-Script': 'foo.bat'],
        'macos':['Application-Script': 'foo-osx.sh'],
        'linux':['Application-Script': 'foo-lin.sh'],
    ]
  }

  @Test
  public void "build nested platform and os manifest attributes"() {
    def m = new CapsuleManifest(application:'foo')
    m.platform('windows') {
      mode('alpha') { systemProperties['x'] = '1' }
      mode('beta') { systemProperties['x'] = '2' }
    }
    m.mode('alpha') {
      platform('MACOS') { systemProperties['x'] = '3' }
      platform('linux') { systemProperties['x'] = '4' }
    }
    def result = m.buildAllManifestAttributes()

    assert result == [
        (null):['Main-Class': 'Capsule', 'Application': 'foo', 'Premain-Class': 'Capsule'],
        'alpha-windows':['System-Properties': 'x=1'],
        'beta-windows':['System-Properties': 'x=2'],
        'alpha-macos':['System-Properties': 'x=3'],
        'alpha-linux':['System-Properties': 'x=4'],
    ]
  }

  @Test
  public void "build manifest skips empty string"(){
    def m = new CapsuleManifest(application:'foo')
    m.javaVersion = ""
    def result = m.buildAllManifestAttributes()

    assert [(null):['Main-Class': 'Capsule', 'Application': 'foo', 'Premain-Class': 'Capsule']] == result
  }

  @Test
  public void "build manifest boolean value"(){
    def m = new CapsuleManifest(application:'foo')
    m.capsuleInClassPath = true
    m.jdkRequired = false
    def result = m.buildAllManifestAttributes()

    assert 'true' == result[null]['Capsule-In-Class-Path']
    assert 'false' == result[null]['JDK-Required']
  }

  @Test
  public void "build manifest list value"(){
    def m = new CapsuleManifest(application:'foo')
    m.jvmArgs.addAll(['hello','manifest', 'world'])
    def result = m.buildAllManifestAttributes()

    assert 'hello manifest world' == result[null]['JVM-Args']
  }

  @Test
  public void "build manifest map value"(){
    def m = new CapsuleManifest(application:'foo')
    m.environmentVariables = [alpha:'Aligator',beta:'Babboon']
    def result = m.buildAllManifestAttributes()

    assert 'alpha=Aligator beta=Babboon' == result[null]['Environment-Variables']
  }

  @Test(expectedExceptions = IllegalArgumentException)
  public void "cannot create mode within java grouping"(){
    new CapsuleManifest().java(7) { mode('x') {} }
  }

  @Test(expectedExceptions = IllegalArgumentException)
  public void "cannot create platform within java grouping"(){
    new CapsuleManifest().java(7) { platform('macos') {} }
  }

  @Test(expectedExceptions = IllegalArgumentException)
  public void "cannot create java within java grouping"(){
    new CapsuleManifest().java(7) { java(8) {} }
  }

  @Test(expectedExceptions = IllegalArgumentException)
  public void "cannot create java within mode grouping"(){
    new CapsuleManifest().mode('x') { java(7) {} }
  }

  @Test(expectedExceptions = IllegalArgumentException)
  public void "cannot create mode within mode grouping"(){
    new CapsuleManifest().mode('x') { mode('y') {} }
  }

  @Test(expectedExceptions = IllegalArgumentException)
  public void "cannot create java within platform grouping"(){
    new CapsuleManifest().platform('linux') { java(7) {} }
  }

  @Test(expectedExceptions = IllegalArgumentException)
  public void "cannot create platform within platform grouping"(){
    new CapsuleManifest().platform('linux') { platform('windows') {} }
  }

  @Test(expectedExceptions = IllegalArgumentException)
  public void "cannot create platform with unknown platform"(){
    new CapsuleManifest().platform('asdf') {}
  }

  @Test(expectedExceptions = IllegalStateException)
  public void "cannot create same platform and mode grouping"(){
    def m = new CapsuleManifest()
    m.platform('linux') { mode('alpha') { args << 'a'} }
    m.mode('alpha') { platform('linux') { args << 'b'} }
    m.buildAllManifestAttributes()
  }

  @Test(expectedExceptions = IllegalStateException)
  public void "build manifest fails without a startup type"(){
    def m = new CapsuleManifest()
    m.buildAllManifestAttributes()
  }

  @Test
  public void "build manifest with dependency config"(){
    def project = ProjectBuilder.builder().build()

    project.with {
      configurations {
        compile
      }

      dependencies {
        compile 'com.foo:Calculator-Core:1.0'
        compile group: 'com.foo', name: 'Calculator-Ajax', version: '1.1', classifier: 'standalone'
        compile('com.foo:Calculator-Mongo:1.2') {
          exclude module: 'common', group: 'com.bar'
          exclude module: 'shared'
        }
        compile(group: 'com.foo', name: 'Calculator-Adware', version: '1.3', classifier: 'annoying') {
          exclude group: 'com.foo.optional'
        }
      }
    }

    def m = new CapsuleManifest(application:'foo')
    m.dependencies << 'fizz:buzz:2000'
    m.dependencyConfiguration = project.configurations.compile
    def result = m.buildAllManifestAttributes()

    def expectedDeps = 'fizz:buzz:2000 ' +
        'com.foo:Calculator-Adware:1.3:annoying(com.foo.optional:*) ' +
        'com.foo:Calculator-Ajax:1.1:standalone ' +
        'com.foo:Calculator-Core:1.0 ' +
        'com.foo:Calculator-Mongo:1.2(com.bar:common,*:shared)'

    assert expectedDeps == result[null]['Dependencies']
  }
}
