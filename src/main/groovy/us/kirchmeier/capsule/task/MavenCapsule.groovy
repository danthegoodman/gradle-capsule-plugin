package us.kirchmeier.capsule.task

class MavenCapsule extends Capsule {

  MavenCapsule() {
    applicationSource = project.tasks.findByName('jar')
    capletConfiguration = project.configurations.mavenCaplet

    capsuleManifest {
      dependencyConfiguration = project.configurations.runtime
      caplets << 'MavenCapsule'
    }
  }
}
