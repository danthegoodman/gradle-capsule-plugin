package us.kirchmeier.capsule.task

class FatCapsule extends Capsule {
  FatCapsule() {
    applicationSource = project.tasks.findByName('jar')
    capsuleFilter = { include 'Capsule.class' }
    embedConfiguration = project.configurations.runtime
  }
}
