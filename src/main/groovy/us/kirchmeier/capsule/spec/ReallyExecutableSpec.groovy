package us.kirchmeier.capsule.spec

import org.gradle.api.Project

class ReallyExecutableSpec {

  File script
  protected boolean _regular = true
  protected boolean _trampoline = false

  ReallyExecutableSpec regular() {
    _regular = true
    _trampoline = false
    script = null
    return this
  }

  ReallyExecutableSpec trampoline() {
    _regular = false
    _trampoline = true
    script = null
    return this
  }

  void setScript(File file) {
    if (file != null) {
      _regular = false
      _trampoline = false
      script = file
    }
  }

  ReallyExecutableSpec script(File file) {
    script = file
    return this
  }

  def buildAntResource(Project project, AntBuilder ant) {
    if (script != null) {
      return ant.file(file: script)
    }

    def cap = project.configurations.capsuleUtil.files.first()

    if (_trampoline) {
      return ant.zipentry(zipfile: cap, name: 'capsule/trampoline-execheader.sh')
    } else if (_regular) {
      return ant.zipentry(zipfile: cap, name: 'capsule/execheader.sh')
    }
  }
}
