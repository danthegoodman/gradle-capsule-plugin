package us.kirchmeier.capsule

import org.gradle.api.Plugin
import org.gradle.api.Project
import us.kirchmeier.capsule.task.Capsule
import us.kirchmeier.capsule.task.FatCapsule
import us.kirchmeier.capsule.task.MavenCapsule

class CapsuleGradlePlugin implements Plugin<Project> {
  private static final REQUIRED_GRADLE_MAJOR_VERSION = 2
  private static final REQUIRED_GRADLE_MINOR_VERSION = 4

  @Override
  void apply(Project project) {
    validateGradleVersion(project)
    project.with {
      apply(plugin: 'java')
      ext.Capsule = Capsule.class
      ext.FatCapsule = FatCapsule.class
      ext.MavenCapsule = MavenCapsule.class

      configurations.create('capsule')
      configurations.create('capsuleUtil')
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
      project.configurations.capsuleUtil.dependencies.clear()
      project.configurations.mavenCaplet.dependencies.clear()
      project.dependencies {
        capsule "co.paralleluniverse:capsule:${version}"
        capsuleUtil "co.paralleluniverse:capsule-util:${version}"
        mavenCaplet "co.paralleluniverse:capsule-maven:${version}"
      }
    }
  }

  void validateGradleVersion(Project project){
    def version = project.gradle.gradleVersion
    def dotNdx = version.indexOf('.')
    if(dotNdx == -1) return;

    int major, minor;
    try{
      major = version.substring(0, dotNdx) as Integer;
      minor = version.substring(dotNdx+1) as Integer;
    } catch(NumberFormatException ignored){
      return;
    }

    if(major > REQUIRED_GRADLE_MAJOR_VERSION) return;
    if(major == REQUIRED_GRADLE_MAJOR_VERSION && minor >= REQUIRED_GRADLE_MINOR_VERSION) return;

    project.logger.error("The capsule plugin requires gradle v${REQUIRED_GRADLE_MAJOR_VERSION}.${REQUIRED_GRADLE_MINOR_VERSION} and above.")
    project.logger.error("You may receieve unexpected errors if you do not upgrade.")
  }
}
