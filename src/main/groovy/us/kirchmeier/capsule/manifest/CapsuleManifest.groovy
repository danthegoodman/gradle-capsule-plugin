package us.kirchmeier.capsule.manifest

import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ModuleDependency

class CapsuleManifest {

  @ManifestSetting('Main-Class')
  public String mainClass = 'Capsule'

  @ManifestSetting('Application-Name')
  public String applicationName

  @ManifestSetting('Application-Version')
  public String applicationVersion

  @ManifestSetting('Application-Class')
  public String applicationClass

  @ManifestSetting('Application')
  public String application

  @ManifestSetting('Unix-Script')
  public String unixScript

  @ManifestSetting('Windows-Script')
  public String windowsScript

  @ManifestSetting('Extract-Capsule')
  public Boolean extractCapsule

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

  @ManifestSetting('Repositories')
  public List<String> repositories = []

  @ManifestSetting('Dependencies')
  public List<String> dependencies = []

  @ManifestSetting('Allow-Snapshots')
  public Boolean allowSnapshots

  @ManifestSetting('Native-Dependencies-Linux')
  public List<String> nativeDependenciesLinux

  @ManifestSetting('Native-Dependencies-Win')
  public List<String> nativeDependenciesWindows

  @ManifestSetting('Native-Dependencies-Mac')
  public List<String> nativeDependenciesMac

  @ManifestSetting('CapsuleLogLevel')
  public String capsuleLogLevel

  @ManifestSetting('Caplets')
  public List<String> caplets = []

  /**
   * The configuration describing the dependencies to list in the manifest.
   * <p>These dependencies are added to the dependency list; they will not overwrite it.</p>
   */
  public Configuration dependencyConfiguration

  public Map<String, String> buildManifestAttributes() {
    def result = [:]
    includeDependencyConfiguration()

    def self = this;
    CapsuleManifest.declaredFields.each { field ->
      def ms = field.getAnnotation(ManifestSetting)
      if (!ms) return

      def value = field.get(self)
      if (canIncludeValue(field.type, value)) {
        result[ms.value()] = formatValue(value)
      }
    }

    validateSettings(result)
    return result
  }

  protected boolean canIncludeValue(Class type, value) {
    if (Boolean.isAssignableFrom(type)) return value != null

    return value.asBoolean()
  }

  protected String formatValue(value) {
    if (value instanceof List) return value.join(' ')
    if (value instanceof Map) return value.collect { "$it.key=$it.value" }.join(' ')

    return value.toString()
  }

  protected includeDependencyConfiguration() {
    if (!dependencyConfiguration) return
    def deps = dependencyConfiguration.allDependencies.collect { d -> formatModuleDependency(d) }.sort()
    dependencies.addAll(deps)
  }

  protected String formatModuleDependency(ModuleDependency d) {
    def result = d.group + ':' + d.name + ':' + d.version + (d.artifacts.empty ? '' : ':' + d.artifacts.first().classifier)
    if (!d.excludeRules.empty) {
      def rules = d.excludeRules.collect { (it.group ?: '*') + ':' + (it.module ?: '*') }
      result += "(" + rules.join(',') + ")"
    }
    return result
  }

  protected void validateSettings(Map result) throws IllegalStateException {
    def main = 'Main-Class'
    if (!result.containsKey(main)) {
      throw new IllegalStateException("Missing required capsule manifest attribute: $main")
    }

    def app = ['Application-Class', 'Application', 'Unix-Script', 'Windows-Script']
    if (app.every { !result.containsKey(it) }) {
      throw new IllegalStateException("Missing required capsule manifest attribute: one of ${app.join(", ")}")
    }
  }
}
