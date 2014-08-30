package us.kirchmeier.capsule.task

import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.bundling.Jar
import org.gradle.util.ConfigureUtil
import us.kirchmeier.capsule.manifest.CapsuleManifest

class Capsule extends Jar {
  /**
   * The dependency configuration describing the main capsule classes.
   * <p>
   * Defaults to <code>configurations.capsule</code>, which has a dependency to the main capsule library.
   * Without a filter, all files from all artifacts in the configuration will be extracted into the capsule.
   * </p><p>
   * If null, you are responsible for including the necessary capsule classes.
   * </p>
   */
  Configuration capsuleConfiguration

  /**
   * A filter, used to limit the capsule classes if the entire jar is not needed.
   * <p>The closure delegates to a {@link org.gradle.api.file.CopySpec}.</p>
   */
  Closure capsuleFilter

  /**
   * The main object to include, representative of the primary application.
   * <p>This object is passed directly to {@link #from(java.lang.Object...)}.</p>
   */
  Object applicationSource

  /**
   * The configuration describing the dependencies to embed in the capsule.
   * <p>If null, no dependencies are embedded.</p>
   */
  Configuration embedConfiguration

  CapsuleManifest capsuleManifest = new CapsuleManifest()

  Capsule() {
    capsuleConfiguration = project.configurations.capsule
    classifier = 'capsule'

    project.gradle.afterProject {
      finalizeSettings()
    }
  }

  public Capsule capsuleManifest(@DelegatesTo(CapsuleManifest) Closure configureClosure) {
    ConfigureUtil.configure(configureClosure, capsuleManifest);
    return this;
  }

  /**
   * Sets the application class on the manifest.
   * @param className The application's fully qualified main class name.
   */
  public Capsule applicationClass(String className){
    capsuleManifest.applicationClass = className;
    return this;
  }

  /**
   * Sets the starting application as a maven dependency.
   * @param application The application as a dependency specification
   */
  public Capsule application(String application){
    capsuleManifest.application = application;
    return this;
  }

  protected void finalizeSettings() {
    applyDefaultCapsuleSet()
    applyApplicationSource()
    applyEmbedConfiguration()

    manifest.attributes(capsuleManifest.buildManifestAttributes())
  }

  protected void applyApplicationSource() {
    if (!applicationSource) return

    from(applicationSource)
  }

  protected void applyDefaultCapsuleSet() {
    if (!capsuleConfiguration) return

    from(capsuleConfiguration.collect({ project.zipTree(it) }), capsuleFilter)
  }

  protected void applyEmbedConfiguration() {
    if (!embedConfiguration) return

    from { embedConfiguration }
  }
}
