package us.kirchmeier.capsule.manifest

import org.gradle.testfixtures.ProjectBuilder
import org.testng.annotations.Test
import us.kirchmeier.capsule.manifest.CapsuleManifest;

public class CapsuleManifestTest {

  @Test
  public void "build manifest attributes with application"() {
    def m = new CapsuleManifest()
    m.application = 'foo:bar'
    def result = m.buildManifestAttributes()

    assert ['Main-Class': 'Capsule', 'Application': 'foo:bar'] == result
  }

  @Test
  public void "build manifest attributes with applicationClass"() {
    def m = new CapsuleManifest()
    m.applicationClass = 'foo.bar'
    def result = m.buildManifestAttributes()

    assert ['Main-Class': 'Capsule', 'Application-Class': 'foo.bar'] == result
  }

  @Test
  public void "build manifest attributes with unixScript"() {
    def m = new CapsuleManifest()
    m.unixScript = 'foo.sh'
    def result = m.buildManifestAttributes()

    assert ['Main-Class': 'Capsule', 'Unix-Script': 'foo.sh'] == result
  }

  @Test
  public void "build manifest attributes with windowsScript"() {
    def m = new CapsuleManifest()
    m.windowsScript = 'foo.exe'
    def result = m.buildManifestAttributes()

    assert ['Main-Class': 'Capsule', 'Windows-Script': 'foo.exe'] == result
  }

  @Test
  public void "build manifest skips empty string"(){
    def m = new CapsuleManifest(application:'foo')
    m.javaVersion = ""
    def result = m.buildManifestAttributes()

    assert ['Main-Class': 'Capsule', 'Application': 'foo'] == result
  }

  @Test
  public void "build manifest boolean value"(){
    def m = new CapsuleManifest(application:'foo')
    m.extractCapsule = true
    m.jdkRequired = false
    def result = m.buildManifestAttributes()

    assert 'true' == result['Extract-Capsule']
    assert 'false' == result['JDK-Required']
  }

  @Test
  public void "build manifest list value"(){
    def m = new CapsuleManifest(application:'foo')
    m.jvmArgs.addAll(['hello','manifest', 'world'])
    def result = m.buildManifestAttributes()

    assert 'hello manifest world' == result['JVM-Args']
  }

  @Test
  public void "build manifest map value"(){
    def m = new CapsuleManifest(application:'foo')
    m.environmentVariables = [alpha:'Aligator',beta:'Babboon']
    def result = m.buildManifestAttributes()

    assert 'alpha=Aligator beta=Babboon' == result['Environment-Variables']
  }

  @Test(expectedExceptions = IllegalStateException)
  public void "build manifest fails without main class"(){
    def m = new CapsuleManifest(application:'foo')
    m.mainClass = null;
    m.buildManifestAttributes()
  }

  @Test(expectedExceptions = IllegalStateException)
  public void "build manifest fails without a startup type"(){
    def m = new CapsuleManifest()
    m.buildManifestAttributes()
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
    def result = m.buildManifestAttributes()

    def expectedDeps = 'fizz:buzz:2000 ' +
        'com.foo:Calculator-Adware:1.3:annoying(com.foo.optional:*) ' +
        'com.foo:Calculator-Ajax:1.1:standalone ' +
        'com.foo:Calculator-Core:1.0 ' +
        'com.foo:Calculator-Mongo:1.2(com.bar:common,*:shared)'

    assert expectedDeps == result['Dependencies']
  }
}
