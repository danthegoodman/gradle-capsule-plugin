package us.kirchmeier.capsule

import org.gradle.api.Plugin
import org.gradle.api.Project
import us.kirchmeier.capsule.task.Capsule
import us.kirchmeier.capsule.task.FatCapsule
import us.kirchmeier.capsule.task.ThinCapsule

class CapsuleGradlePlugin implements Plugin<Project> {

  @Override
  void apply(Project project) {
    project.with {
      apply(plugin: 'java')
      ext.Capsule = Capsule.class
      ext.FatCapsule = FatCapsule.class
      ext.ThinCapsule = ThinCapsule.class

      configurations.create('capsule')
      dependencies {
        capsule 'co.paralleluniverse:capsule:0.9.0'
      }
    }
  }
}
