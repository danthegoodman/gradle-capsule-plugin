package us.kirchmeier.capsule

import org.gradle.api.Plugin
import org.gradle.api.Project
import us.kirchmeier.capsule.task.Capsule
import us.kirchmeier.capsule.task.FatCapsule
import us.kirchmeier.capsule.task.MavenCapsule

class CapsuleGradlePlugin implements Plugin<Project> {

  @Override
  void apply(Project project) {
    project.with {
      apply(plugin: 'java')
      ext.Capsule = Capsule.class
      ext.FatCapsule = FatCapsule.class
      ext.MavenCapsule = MavenCapsule.class

      configurations.create('capsule')
      configurations.create('mavenCaplet')
      configurations.create('caplet')

      configurations.mavenCaplet.extendsFrom(configurations.caplet)

      def capsuleExt = project.extensions.create("capsule", CapsuleGradleExtension, project)
      capsuleExt.version = '1.0'
    }
  }

  static class CapsuleGradleExtension {
    Project project

    CapsuleGradleExtension(Project project){
      this.project = project
    }

    void setVersion(String version){
      project.configurations.capsule.dependencies.clear()
      project.configurations.mavenCaplet.dependencies.clear()
      project.dependencies {
        capsule "co.paralleluniverse:capsule:${version}"
        mavenCaplet "co.paralleluniverse:capsule-maven:${version}"
      }
    }
  }
}
