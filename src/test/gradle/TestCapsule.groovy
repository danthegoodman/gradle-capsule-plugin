import java.nio.file.Files
import java.util.jar.Attributes
import java.util.jar.JarFile

testContents_Fat('fatCapsule')
testManifest_Fat('fatCapsule')
testOutput_Capsule('fatCapsule')

testContents_Fat('fatCapsuleExecutable')
testManifest_Fat('fatCapsuleExecutable')
testExecutableHeader('fatCapsuleExecutable')
testOutput_Executable('fatCapsuleExecutable')

testContents_Fat('platformCapsule')
testManifest_Fat('platformCapsule')
testOutput_Capsule('platformCapsule')

testContents_Fat('customExecutableScript')
testManifest_Fat('customExecutableScript')
testExecutableHeader_Custom('customExecutableScript')
testOutput_CustomExecutable('customExecutableScript')

testContents_Fat('fatCapsuleInSubproject', ['subproject.jar'])
testManifest_Fat('fatCapsuleInSubproject')
testOutput_Capsule('fatCapsuleInSubproject')

testContents_Maven('mavenCapsule')
testManifest_Maven('mavenCapsule')
testOutput_Capsule('mavenCapsule')

testContents_Maven('mavenCapsuleExecutable')
testManifest_Maven('mavenCapsuleExecutable')
testExecutableHeader('mavenCapsuleExecutable')
testOutput_Executable('mavenCapsuleExecutable')

testContents_Maven('recreatedMavenCapsule')
testManifest_Maven('recreatedMavenCapsule')
testOutput_Capsule('recreatedMavenCapsule')



void testContents_Fat(name, additionalContents = []) {
  println "$name: test contents"
  def f = new JarFile(capsuleFile(name), false)
  def projectFiles = f.entries().collect { it.name }

  def expectedFiles = [
      'Capsule.class',
      'META-INF/',
      'META-INF/MANIFEST.MF',
      'settings.gradle',
      'test-project.jar',
      '1-commons-1.0.0.jar',
      'ant-1.9.3.jar',
      'commons-1.0.0.jar',
      'hamcrest-core-1.3.jar',
      'junit-4.11.jar',
  ]
  expectedFiles.addAll(additionalContents)

  projectFiles.sort()
  expectedFiles.sort()
  assert projectFiles == expectedFiles
}

void testContents_Maven(name) {
  println "$name: test contents"
  def f = new JarFile(capsuleFile(name), false)
  def projectFiles = f.entries().collect { it.name } findAll { !it.startsWith('capsule/') }

  def expectedFiles = [
      'MavenCapsule.class',
      'Capsule.class',
      'META-INF/',
      'META-INF/MANIFEST.MF',
      'settings.gradle',
      'test-project.jar',
  ]

  projectFiles.sort()
  expectedFiles.sort()
  assert projectFiles == expectedFiles
}



void testManifest_Fat(name){
  println "$name: test manifest"
  def jm = readAndCheckCommonManifest(name)
  assert jm.size() == 4
}

void testManifest_Maven(name){
  println "$name: test manifest"
  def jm = readAndCheckCommonManifest(name)
  assert jm.getValue('Dependencies') == 'com.github.penggle:commons:1.0.0(*:*) com.sefford:commons:1.0.0(*:*) junit:junit:4.11 org.apache.ant:ant:1.9.3(*:ant-launcher)'
  assert jm.getValue('Caplets') == 'MavenCapsule'
  assert jm.size() == 6
}

Attributes readAndCheckCommonManifest(name) {
  def f = new JarFile(capsuleFile(name), false)
  def jm = f.manifest.mainAttributes
  assert jm.getValue('Manifest-Version') != null
  assert jm.getValue('Premain-Class') == 'Capsule'
  assert jm.getValue('Main-Class') == 'Capsule'
  assert jm.getValue('Application-Class') == 'com.foo.Main'
  return jm;
}



void testExecutableHeader(name){
  println "$name: test has executable header"
  def file = capsuleFile(name)
  assert file.readLines()[0] == "#!/bin/sh"
}

void testExecutableHeader_Custom(name){
  println "$name: test has executable header"
  def file = capsuleFile(name)
  def expectedHeader = new File("really-executable.base.sh").readLines()
  def actualHeader = file.readLines()[0..<expectedHeader.size()]
  assert actualHeader == expectedHeader
}




void testOutput_Capsule(name) {
  println "$name: test output"
  def cmd = ['java', '-jar', capsuleFile(name).absolutePath]
  def strOut = readOutput(cmd)
  assert strOut == 'Hello World\n'
}

void testOutput_Executable(name) {
  println "$name: test output"
  def cmd = [capsuleFile(name).absolutePath]
  def strOut = readOutput(cmd)
  assert strOut == 'Hello World\n'
}

void testOutput_CustomExecutable(name) {
  println "$name: test output"
  def cmd = [capsuleFile(name).absolutePath]
  def strOut = readOutput(cmd)
  assert strOut == 'Base!\nHello World\n'
}

private String readOutput(command){
  def env = System.getenv().collect{ k,v -> "$k=$v" }
  env << "CAPSULE_CACHE_DIR=${Files.createTempDirectory("capsule_test")}";
  def proc = command.execute(env, null)
  def out = new ByteArrayOutputStream()
  proc.consumeProcessOutput(out, System.err)
  int exitCode = proc.waitFor()
  assert exitCode == 0
  return new String(out.toByteArray());
}



private File capsuleFile(String name){
  return new File("build/capsules/${name}.jar")
}