package us.kirchmeier.capsule.task

class FatCapsule extends Capsule {
  FatCapsule() {
    applicationSource = project.tasks.findByName('jar')
    embedConfiguration = project.configurations.runtime
  }
}
