package us.kirchmeier.capsule.manifest

import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.util.ConfigureUtil

class CapsuleManifest {

  @ManifestSetting('Premain-Class')
  public String premainClass

  @ManifestSetting('Main-Class')
  public String mainClass

  @ManifestSetting('Application-Name')
  public String applicationName

  @ManifestSetting('Application-ID')
  public String applicationId

  @ManifestSetting('Application-Version')
  public String applicationVersion

  @ManifestSetting('Application-Class')
  public String applicationClass

  @ManifestSetting('Application')
  public String application

  @ManifestSetting('Application-Script')
  public String applicationScript

  @ManifestSetting('Min-Java-Version')
  public String minJavaVersion

  @ManifestSetting('Min-Update-Version')
  public Map<String, String> minUpdateVersion = [:]

  @ManifestSetting('Java-Version')
  public String javaVersion

  @ManifestSetting('JDK-Required')
  public Boolean jdkRequired

  @ManifestSetting('JVM-Args')
  public List<String> jvmArgs = []

  @ManifestSetting('Args')
  public List<String> args = []

  @ManifestSetting('Environment-Variables')
  public Map<String, String> environmentVariables = [:]

  @ManifestSetting('System-Properties')
  public Map<String, String> systemProperties = [:]

  @ManifestSetting('App-Class-Path')
  public List<String> appClassPath = []

  @ManifestSetting('Capsule-In-Class-Path')
  public Boolean capsuleInClassPath

  @ManifestSetting('Boot-Class-Path')
  public List<String> bootClassPath = []

  @ManifestSetting('Boot-Class-Path-A')
  public List<String> bootClassPathAppended = []

  @ManifestSetting('Boot-Class-Path-P')
  public List<String> bootClassPathPrepended = []

  @ManifestSetting('Library-Path-A')
  public List<String> libraryPathAppended = []

  @ManifestSetting('Library-Path-P')
  public List<String> libraryPathPrepended = []

  @ManifestSetting('Security-Manager')
  public String securityManager

  @ManifestSetting('Security-Policy')
  public String securityPolicy

  @ManifestSetting('Security-Policy-A')
  public String securityPolicyAppended

  @ManifestSetting('Java-Agents')
  public List<String> javaAgents = []

  @ManifestSetting('Native-Agents')
  public List<String> nativeAgents = []

  @ManifestSetting('Repositories') //Used only by MavenCapsule
  public List<String> repositories = []

  @ManifestSetting('Allow-Snapshots')  //Used only by MavenCapsule
  public Boolean allowSnapshots

  @ManifestSetting('Dependencies')
  public List<String> dependencies = []

  @ManifestSetting('Native-Dependencies')
  public List<String> nativeDependencies

  @ManifestSetting('Capsule-Log-Level')
  public String capsuleLogLevel

  @ManifestSetting('Caplets')
  public List<String> caplets = []

  @ManifestSetting('Description')
  public String modeDescription

  public LinkedHashMap<String, String> customEntries;

  /**
   * The configuration describing the dependencies to list in the manifest.
   * <p>These dependencies are added to the dependency list; they will not overwrite it.</p>
   */
  public Configuration dependencyConfiguration

  public CapsuleManifest dependencyConfiguration(Configuration dependencyConfiguration) {
    this.dependencyConfiguration = dependencyConfiguration
    return this
  }

  public void mode(String name, @DelegatesTo(CapsuleManifest) Closure configClosure){
    if(!name) throw new IllegalArgumentException("Capsule mode must be non-empty and non-null");
    if(groupMode) throw new IllegalArgumentException("Cannot overwrite capsule mode ${groupMode} with ${name}");
    if(groupJavaVersion) throw new IllegalArgumentException("Cannot combine java specific properties with other groupings");

    def m = new CapsuleManifest(groupMode: name, groupPlatform: this.groupPlatform)
    children << m
    ConfigureUtil.configure(configClosure, m);
  }

  public void platform(String platform, @DelegatesTo(CapsuleManifest) Closure configClosure){
    if(!platform) throw new IllegalArgumentException("Capsule platform must be non-empty and non-null");
    platform = platform.toLowerCase();
    if(!PLATFORMS.contains(platform)) throw new IllegalArgumentException("Capsule platform is invalid: ${platform} ; it must be one of ${PLATFORMS}")
    if(groupPlatform) throw new IllegalArgumentException("Cannot overwrite capsule platofmr ${groupPlatform} with ${platform}");
    if(groupJavaVersion) throw new IllegalArgumentException("Cannot combine java specific properties with other groupings");

    def m = new CapsuleManifest(groupMode: this.groupMode, groupPlatform: platform)
    children << m
    ConfigureUtil.configure(configClosure, m);
  }

  public void java(int version, @DelegatesTo(CapsuleManifest) Closure configClosure){
    if(groupJavaVersion) throw new IllegalArgumentException("Cannot overwrite capsule java grouping ${groupJavaVersion} with ${version}")
    if(groupMode || groupPlatform) throw new IllegalArgumentException("Cannot combine java specific properties with other groupings");

    def m = new CapsuleManifest(groupJavaVersion: Integer.toString(version))
    children << m
    ConfigureUtil.configure(configClosure, m);
  }

  /**
   * Builds manifest attributes for this grouping and all subgroupings.
   *
   * @return Map of group names to a map of attributes. The primary group name will be null.
   */
  public Map<String, Map<String, String>> buildAllManifestAttributes(){
    def result = new HashMap<String, Map<String,String>>();
    allManifests.each {
      def name = it.buildGroupName();
      def attrs = it.buildManifestAttributes()
      if(name && !attrs) return
      if(result.containsKey(name)) throw new IllegalStateException("Cannot resolve capsule attributes for duplicate grouping '${name}'");
      result[name] = attrs;
    }
    result[null].get('Premain-Class', 'Capsule');
    result[null].get('Main-Class', 'Capsule');
    validateSettings(result)
    return result;
  }

  private static final OS_WINDOWS = 'windows';
  private static final OS_MACOS = 'macos';
  private static final OS_LINUX = 'linux';
  private static final OS_SOLARIS = 'solaris';
  private static final OS_UNIX = 'unix';
  private static final OS_POSIX = 'posix';
  private static final PLATFORMS = [OS_WINDOWS, OS_MACOS, OS_LINUX, OS_SOLARIS, OS_UNIX, OS_POSIX] as String[];

  private String groupMode = null;
  private String groupPlatform = null;
  private String groupJavaVersion = null;
  private List<CapsuleManifest> children = [];

  private String buildGroupName(){
    def parts = [];
    if(groupMode) parts.add(groupMode)
    if(groupPlatform) parts.add(groupPlatform)
    if(groupJavaVersion) parts.add("java-$groupJavaVersion")
    if(!parts) return null;
    return parts.join('-')
  }

  private List<CapsuleManifest> getAllManifests(){
    return (children*.allManifests + this).flatten()
  }

  private Map<String, String> buildManifestAttributes() {
    def result = [:]
    includeDependencyConfiguration()

    def self = this;
    getClass().fields.each { field ->
      def ms = field.getAnnotation(ManifestSetting)
      if (!ms) return

      def value = field.get(self)
      if (canIncludeValue(field.type, value)) {
        result[ms.value()] = formatValue(value)
      }
    }

    self.customEntries.each {
      result[it.key] = it.value
    }

    return result
  }

  private boolean canIncludeValue(Class type, value) {
    if (Boolean.isAssignableFrom(type)) return value != null

    return value.asBoolean()
  }

  private String formatValue(value) {
    if (value instanceof List) return value.join(' ')
    if (value instanceof Map) return value.collect { "$it.key=$it.value" }.join(' ')

    return value.toString()
  }

  private includeDependencyConfiguration() {
    if (!dependencyConfiguration) return
    def deps = dependencyConfiguration.allDependencies
        .findAll { it instanceof ModuleDependency }
        .collect { d -> formatModuleDependency(d) }
        .sort()
    dependencies.addAll(deps)
  }

  private String formatModuleDependency(ModuleDependency d) {
    def result = d.group + ':' + d.name + ':' + d.version + (d.artifacts.empty ? '' : ':' + d.artifacts.first().classifier)
    if (!d.excludeRules.empty) {
      def rules = d.excludeRules.collect { (it.group ?: '*') + ':' + (it.module ?: '*') }
      result += "(" + rules.join(',') + ")"
    }
    return result
  }

  private void validateSettings(Map<String,Map> result) throws IllegalStateException {
    def app = ['Application-Class', 'Application', 'Application-Script']
    if (!result.any { k, v -> app.any { appKey -> v.containsKey(appKey) } }) {
      throw new IllegalStateException("Missing required capsule manifest attribute: one of ${app.join(", ")}")
    }
  }
}
