package us.kirchmeier.capsule.spec

import org.gradle.api.Project
import org.gradle.api.tasks.TaskInputs

class ReallyExecutableSpec {

  File script
  protected boolean _regular = true
  protected boolean _trampoline = false
  protected TaskInputs _taskInputs
  
  ReallyExecutableSpec(TaskInputs taskInputs) {
    _taskInputs = taskInputs
  }

  ReallyExecutableSpec regular() {
    _regular = true
    _trampoline = false
    script = null
    return this
  }

  /**
   * Deprecated because this conflicts with the closure method 'trampoline()'
   *
   * @deprecated Use trampolining() instead
   * @return
   */
  @Deprecated
  ReallyExecutableSpec trampoline() {
    return this.trampolining()
  }

  ReallyExecutableSpec trampolining() {
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
    _taskInputs.file script
    return this
  }

  def buildAntResource(Project project, AntBuilder ant) {
    if (script != null) {
      if(!script.exists()) {
        throw new IllegalArgumentException("Unable to locate reallyExecutable script: ${script}");
      }
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
