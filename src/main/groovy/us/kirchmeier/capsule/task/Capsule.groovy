package us.kirchmeier.capsule.task

import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.FileCollectionDependency
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.tasks.bundling.Jar
import org.gradle.util.ConfigureUtil
import us.kirchmeier.capsule.manifest.CapsuleManifest
import us.kirchmeier.capsule.spec.ReallyExecutableSpec

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
  Closure capsuleFilter = {}

  /**
   * The caplet configuration describing any caplet classes.
   * <p>
   * Defaults to <code>configurations.caplet</code>. All files not named 'Capsule.class'
   * will be extracted into the capsule.
   * </p><p>
   * If null, you are responsible for including the necessary caplet classes.
   * </p>
   */
  Configuration capletConfiguration

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

  protected ReallyExecutableSpec _reallyExecutable = null
  protected HashMap<String,Integer> _embeddedNameCounter = new HashMap<String,Integer>()

  Capsule() {
    capsuleConfiguration = project.configurations.capsule
    capletConfiguration = project.configurations.caplet
    classifier = 'capsule'

    project.afterEvaluate {
      finalizeSettings()
      if(_reallyExecutable != null){
        doLast { makeReallyExecutable() }
      }
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

  public Capsule capsuleConfiguration(Configuration capsuleConfiguration) {
    this.capsuleConfiguration = capsuleConfiguration
    return this
  }

  public Capsule capletConfiguration(Configuration capletConfiguration) {
    this.capletConfiguration = capletConfiguration
    return this
  }

  public Capsule embedConfiguration(Configuration embedConfiguration){
    this.embedConfiguration = embedConfiguration
    return this
  }

  public ReallyExecutableSpec getReallyExecutable(){
    if(_reallyExecutable == null){
      _reallyExecutable = new ReallyExecutableSpec(inputs);
    }
    return _reallyExecutable;
  }

  public void setReallyExecutable(ReallyExecutableSpec spec) {
    _reallyExecutable = spec
  }

  public Capsule reallyExecutable(@DelegatesTo(ReallyExecutableSpec) Closure configureClosure) {
     ConfigureUtil.configure(configureClosure, getReallyExecutable());
     return this;
  }

  protected void finalizeSettings() {
    applyDefaultCapsuleSet()
    applyDefaultCapletSet()
    applyApplicationSource()
    applyEmbedConfiguration()
    applyFileDependenciesFromManifestDependencies()

    def attrs = capsuleManifest.buildAllManifestAttributes();
    attrs.each { k, v ->
      if(k){
        manifest.attributes(v, k)
      } else {
        manifest.attributes(v)
      }
    }
  }

  protected void applyApplicationSource() {
    if (!applicationSource) return

    from(applicationSource)
  }

  protected void applyDefaultCapsuleSet() {
    if (!capsuleConfiguration) return

    from(capsuleConfiguration.collect({ project.zipTree(it) }), capsuleFilter)
  }

  protected void applyDefaultCapletSet() {
    if (!capletConfiguration) return

    from(capletConfiguration.collect({ project.zipTree(it) }), { exclude 'Capsule.class'} )
  }

  protected void applyEmbedConfiguration() {
    if (!embedConfiguration) return

    from(embedConfiguration){
      rename { getNonConflictingEmbeddedName(it) }
    }
  }

  protected void applyFileDependenciesFromManifestDependencies() {
    def dc = capsuleManifest.dependencyConfiguration
    if (!dc) return

    def files = dc.allDependencies
        .findAll { it instanceof FileCollectionDependency }
        .collectMany { FileCollectionDependency d -> d.resolve() }
    if(!files) return

    from(files){
      rename { getNonConflictingEmbeddedName(it) }
    }
  }

  protected void makeReallyExecutable() {
    def f = File.createTempFile("cap", null)
    ant.concat(destfile: f, binary: true) {
      _reallyExecutable.buildAntResource(project, ant)
      fileset(dir: destinationDir) {
        include(name: archiveName)
      }
    }
    ant.chmod(file: f, perm: 'ug+x', osfamily: 'unix')
    ant.move(file: f, tofile: outputs.files.singleFile)
  }

  protected String getNonConflictingEmbeddedName(String name){
    def i = _embeddedNameCounter.get(name, -1) + 1
    _embeddedNameCounter[name] = i
    if(i == 0) return name
    return "$i-$name"
  }
}
