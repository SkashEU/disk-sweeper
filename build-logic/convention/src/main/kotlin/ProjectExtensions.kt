import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.compose.ComposeExtension
import kotlin.jvm.optionals.getOrNull

private const val FALLBACK_JVM_VERSION = 21

val Project.versionCatalog
    get(): VersionCatalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

val Project.JVM_TOOLCHAIN_VERSION
    get() =
        versionCatalog
            .findIntVersion("jvm-toolchain")
            ?: FALLBACK_JVM_VERSION

val Project.composeExtension: ComposeExtension
    get() = extensions.findByType(ComposeExtension::class.java)
        ?: extensions.create("composeInternal", ComposeExtension::class.java, this)

fun Project.applyPluginsByName(vararg names: String) =
    names
        .map { versionCatalog.findPlugin(it) }
        .mapNotNull { it.getOrNull()?.orNull?.pluginId }
        .forEach { pluginManager.apply(it) }

fun VersionCatalog.findIntVersion(versionName: String): Int? =
    findVersion(versionName)
        .getOrNull()
        ?.requiredVersion
        ?.toIntOrNull()

fun Project.findLibraryByName(
    nameInVersionCatalog: String
): Provider<MinimalExternalModuleDependency> = project.versionCatalog.findLibrary(nameInVersionCatalog).get()
