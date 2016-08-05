
println "Checking to see if customExecutable capsule was rebuilt"
testExecutableHeader_Custom("customExecutableScript")

private File capsuleFile(String name){
  return new File("build/capsules/${name}.jar")
}

void testExecutableHeader_Custom(name){
  println "$name: test has executable header"
  def file = capsuleFile(name)
  def expectedHeader = new File("really-executable.modified.sh").readLines()
  def actualHeader = file.readLines()[0..<expectedHeader.size()]
  assert actualHeader == expectedHeader
}
