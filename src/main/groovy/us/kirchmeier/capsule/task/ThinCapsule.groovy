package us.kirchmeier.capsule.task

class ThinCapsule extends Capsule {
  ThinCapsule() {
    applicationSource = project.sourceSets.main.output
    capsuleManifest {
      dependencyConfiguration = project.configurations.runtime
    }
  }
}
